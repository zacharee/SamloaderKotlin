package tk.zwander.commonCompose.view.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyVerticalStaggeredGridScrollbar
import tk.zwander.common.tools.delegates.History
import tk.zwander.common.util.invoke
import tk.zwander.commonCompose.locals.LocalDecryptModel
import tk.zwander.commonCompose.locals.LocalDownloadModel
import tk.zwander.commonCompose.locals.LocalHistoryModel
import tk.zwander.commonCompose.util.ThemeConstants
import tk.zwander.commonCompose.util.grid.AdaptiveFixed
import tk.zwander.commonCompose.view.LocalMenuBarHeight
import tk.zwander.commonCompose.view.LocalPagerState
import tk.zwander.commonCompose.view.components.HistoryItem
import tk.zwander.commonCompose.view.components.HybridButton
import tk.zwander.commonCompose.view.components.MRFLayout
import tk.zwander.commonCompose.view.components.Page
import tk.zwander.samloaderkotlin.resources.MR

/**
 * The History View.
 */
@Composable
internal fun HistoryView() {
    val model = LocalHistoryModel.current
    val downloadModel = LocalDownloadModel.current
    val decryptModel = LocalDecryptModel.current
    val pagerState = LocalPagerState.current
    val contentColor = LocalContentColor.current
    val primaryColor = MaterialTheme.colorScheme.primary

    val scope = rememberCoroutineScope()
    val hasRunningJobs by model.hasRunningJobs.collectAsState(false)
    val modelModel by model.model.collectAsState()
    val region by model.region.collectAsState()
    val canCheckHistory = modelModel.isNotBlank()
            && region.isNotBlank() && !hasRunningJobs

    val historySource = buildAnnotatedString {
        withStyle(
            SpanStyle(
                color = contentColor,
                fontSize = 16.sp,
            ),
        ) {
            append(MR.strings.source())
            append(" ")

            withStyle(
                SpanStyle(
                    color = primaryColor,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                withLink(LinkAnnotation.Url("https://samfrew.com")) {
                    append(MR.strings.samfrew())
                }
            }
        }
    }

    val historyItems by model.historyItems.collectAsState()
    val changelogs by model.changelogs.collectAsState()

    val expanded = remember {
        mutableStateMapOf<String, Boolean>()
    }

    val gridState = rememberLazyStaggeredGridState()

    LazyVerticalStaggeredGridScrollbar(
        state = gridState,
        settings = ThemeConstants.ScrollBarSettings.Default,
    ) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            columns = AdaptiveFixed(minSize = 350.dp),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                bottom = 8.dp,
                top = 8.dp + LocalMenuBarHeight.current,
            ),
            state = gridState,
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val constraints = constraints

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            HybridButton(
                                onClick = {
                                    model.historyItems.value = listOf()

                                    model.launchJob {
                                        History.onFetch(model)
                                    }
                                },
                                enabled = canCheckHistory,
                                text = stringResource(MR.strings.checkHistory),
                                description = stringResource(MR.strings.checkHistory),
                                vectorIcon = painterResource(MR.images.refresh),
                                parentSize = constraints.maxWidth
                            )

                            Spacer(Modifier.weight(1f))

                            HybridButton(
                                onClick = {
                                    model.endJob("")
                                },
                                enabled = hasRunningJobs,
                                text = stringResource(MR.strings.cancel),
                                description = stringResource(MR.strings.cancel),
                                vectorIcon = painterResource(MR.images.cancel),
                                parentSize = constraints.maxWidth
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    MRFLayout(model, !hasRunningJobs, !hasRunningJobs, false)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = historySource,
                            style = LocalTextStyle.current.copy(LocalContentColor.current),
                        )
                    }

                    AnimatedVisibility(
                        visible = hasRunningJobs,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Spacer(modifier = Modifier.size(8.dp))

                            LinearProgressIndicator(
                                modifier = Modifier.height(16.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                        }
                    }
                }
            }

            itemsIndexed(historyItems, { _, item -> item.firmwareString }) { index, historyInfo ->
                HistoryItem(
                    index = index,
                    info = historyInfo,
                    changelog = changelogs?.changelogs?.get(historyInfo.firmwareString.split("/")[0]),
                    changelogExpanded = expanded[historyInfo.toString()] ?: false,
                    onChangelogExpanded = { expanded[historyInfo.toString()] = it },
                    onDownload = {
                        downloadModel.manual.value = true
                        downloadModel.osCode.value = ""
                        downloadModel.model.value = model.model.value
                        downloadModel.region.value = model.region.value
                        downloadModel.fw.value = it

                        scope.launch {
                            pagerState.animateScrollToPage(Page.Downloader)
                        }
                    },
                    onDecrypt = {
                        decryptModel.fileToDecrypt.value = null
                        decryptModel.model.value = model.model.value
                        decryptModel.region.value = model.region.value
                        decryptModel.fw.value = it

                        scope.launch {
                            pagerState.animateScrollToPage(Page.Decrypter)
                        }
                    },
                    modifier = Modifier.animateItem(fadeOutSpec = null),
                )
            }
        }
    }
}
