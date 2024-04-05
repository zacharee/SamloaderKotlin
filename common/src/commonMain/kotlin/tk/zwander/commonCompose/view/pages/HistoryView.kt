package tk.zwander.commonCompose.view.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleeksoft.ksoup.Ksoup
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import korlibs.time.DateFormat
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyStaggeredGridVerticalScrollbarNew
import my.nanihadesuka.compose.ScrollbarSelectionMode
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.util.ChangelogHandler
import tk.zwander.common.util.CrossPlatformBugsnag
import tk.zwander.common.util.UrlHandler
import tk.zwander.common.util.firstElementByTagName
import tk.zwander.common.util.getFirmwareHistoryString
import tk.zwander.common.util.getFirmwareHistoryStringFromSamsung
import tk.zwander.common.util.invoke
import tk.zwander.common.util.makeFirmwareString
import tk.zwander.commonCompose.locals.LocalDecryptModel
import tk.zwander.commonCompose.locals.LocalDownloadModel
import tk.zwander.commonCompose.locals.LocalHistoryModel
import tk.zwander.commonCompose.model.HistoryModel
import tk.zwander.commonCompose.util.ThemeConstants
import tk.zwander.commonCompose.view.LocalPagerState
import tk.zwander.commonCompose.view.components.HistoryItem
import tk.zwander.commonCompose.view.components.HybridButton
import tk.zwander.commonCompose.view.components.MRFLayout
import tk.zwander.commonCompose.view.components.Page
import tk.zwander.samloaderkotlin.resources.MR

private fun parseHistoryXml(xml: String): List<HistoryInfo> {
    val doc = Ksoup.parse(xml)

    val latest = doc.firstElementByTagName("firmware")?.firstElementByTagName("version")?.firstElementByTagName("latest")
    val historical = doc.firstElementByTagName("firmware")?.firstElementByTagName("version")?.firstElementByTagName("upgrade")
        ?.getElementsByTag("value")

    println(historical)

    val items = arrayListOf<HistoryInfo>()

    fun parseFirmware(string: String): String {
        val firmwareParts = string.split("/").filterNot { it.isBlank() }.toMutableList()

        if (firmwareParts.size == 2) {
            firmwareParts.add(firmwareParts[0])
            firmwareParts.add(firmwareParts[0])
        }

        if (firmwareParts.size == 3) {
            firmwareParts.add(firmwareParts[0])
        }

        return firmwareParts.joinToString("/")
    }

    latest?.apply {
        val androidVersion = latest.attribute("o")?.value

        items.add(
            HistoryInfo(
                date = null,
                androidVersion = androidVersion,
                firmwareString = parseFirmware(latest.text())
            )
        )
    }

    historical?.apply {
        items.addAll(
            mapNotNull {
                val firmware = parseFirmware(it.text())

                if (firmware.isNotBlank()) {
                    HistoryInfo(
                        date = null,
                        androidVersion = null,
                        firmwareString = firmware
                    )
                } else {
                    null
                }
            }.sortedByDescending {
                it.firmwareString.let { f ->
                    f.substring(f.lastIndex - 3)
                }
            }
        )
    }

    return items
}

private fun parseHistory(body: String): List<HistoryInfo> {
    val doc = Ksoup.parse(body)

    val listItems = doc.select(".index_list").apply {
        removeAt(0)
    }

    return listItems.map {
        val cols = it.select(".index_body_list")
        val date = cols[6].text()
        val version = cols[5].text()

        val link = cols[0].children()[0].children().attr("href")
        val split = link.split("-")

        val pda = split[split.lastIndex - 1]
        val csc = split[split.lastIndex].split(".")[0]

        val formats = arrayOf(
            "yyyy/M/d",
            "yyyy-M-d",
            "M/d/yyyy"
        )

        val parsed = formats.firstNotNullOfOrNull { format ->
            try {
                DateFormat(format).tryParse(date)
            } catch (e: Exception) {
                null
            }
        }

        HistoryInfo(
            parsed ?: throw IllegalArgumentException("Invalid date format $date"),
            version,
            makeFirmwareString(pda, csc)
        )
    }
}

private suspend fun onFetch(model: HistoryModel) {
    try {
        val historyString = getFirmwareHistoryString(model.model.value ?: "", model.region.value ?: "")
        val historyStringXml = getFirmwareHistoryStringFromSamsung(model.model.value ?: "", model.region.value ?: "")

        if (historyString == null && historyStringXml == null) {
            model.endJob(MR.strings.historyError())
        } else {
            try {
                val parsed = when {
                    historyString != null -> {
                        parseHistory(historyString)
                    }

                    historyStringXml != null -> {
                        parseHistoryXml(historyStringXml)
                    }

                    else -> {
                        model.endJob(MR.strings.historyError())
                        return
                    }
                }

                model.changelogs.value = try {
                    ChangelogHandler.getChangelogs(model.model.value ?: "", model.region.value ?: "")
                } catch (e: Exception) {
                    println("Error retrieving changelogs")
                    e.printStackTrace()
                    null
                }
                model.historyItems.value = parsed
                model.endJob("")
            } catch (e: Exception) {
                e.printStackTrace()
                model.endJob(MR.strings.historyErrorFormat(e.message.toString()))
            }
        }
    } catch (e: Throwable) {
        CrossPlatformBugsnag.notify(e)

        model.endJob("${MR.strings.historyError()}${e.message?.let { "\n\n$it" }}")
    }
}

/**
 * The History View.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HistoryView() {
    val model = LocalHistoryModel.current
    val downloadModel = LocalDownloadModel.current
    val decryptModel = LocalDecryptModel.current
    val pagerState = LocalPagerState.current

    val scope = rememberCoroutineScope()
    val hasRunningJobs by model.hasRunningJobs.collectAsState(false)
    val modelModel by model.model.collectAsState()
    val region by model.region.collectAsState()
    val statusText by model.statusText.collectAsState()
    val canCheckHistory = !modelModel.isNullOrBlank()
            && !region.isNullOrBlank() && !hasRunningJobs

    val odinRomSource = buildAnnotatedString {
        pushStyle(
            SpanStyle(
                color = LocalContentColor.current,
                fontSize = 16.sp
            )
        )
        append(MR.strings.source())
        append(" ")
        pushStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        )
        pushStringAnnotation("OdinRomLink", "https://odinrom.com")
        append(MR.strings.odinRom())
        pop()
    }

    val historyItems by model.historyItems.collectAsState()
    val changelogs by model.changelogs.collectAsState()

    val expanded = remember {
        mutableStateMapOf<String, Boolean>()
    }

    val gridState = rememberLazyStaggeredGridState()

    LazyStaggeredGridVerticalScrollbarNew(
        state = gridState,
        thumbColor = ThemeConstants.Colors.scrollbarUnselected,
        thumbSelectedColor = ThemeConstants.Colors.scrollbarSelected,
        alwaysShowScrollBar = true,
        padding = ThemeConstants.Dimensions.scrollbarPadding,
        thickness = ThemeConstants.Dimensions.scrollbarThickness,
        selectionMode = ScrollbarSelectionMode.Disabled,
    ) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            columns = StaggeredGridCells.Adaptive(minSize = 350.dp),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp),
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
                                        onFetch(model)
                                    }
                                },
                                enabled = canCheckHistory,
                                text = stringResource(MR.strings.checkHistory),
                                description = stringResource(MR.strings.checkHistory),
                                vectorIcon = painterResource(MR.images.refresh),
                                parentSize = constraints.maxWidth
                            )

                            if (hasRunningJobs) {
                                Spacer(Modifier.width(8.dp))

                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp)
                                        .align(Alignment.CenterVertically),
                                    strokeWidth = 2.dp
                                )
                            }

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
                        ClickableText(
                            text = odinRomSource,
                            modifier = Modifier.padding(start = 4.dp),
                            onClick = {
                                odinRomSource.getStringAnnotations("OdinRomLink", it, it)
                                    .firstOrNull()?.let { item ->
                                        UrlHandler.launchUrl(item.item)
                                    }
                            },
                            style = LocalTextStyle.current.copy(LocalContentColor.current),
                        )

                        Spacer(Modifier.weight(1f))

                        Text(text = statusText)
                    }
                }
            }

            itemsIndexed(historyItems, { _, item -> item.toString() }) { index, historyInfo ->
                HistoryItem(
                    index = index,
                    info = historyInfo,
                    changelog = changelogs?.changelogs?.get(historyInfo.firmwareString.split("/")[0]),
                    changelogExpanded = expanded[historyInfo.toString()] ?: false,
                    onChangelogExpanded =  { expanded[historyInfo.toString()] = it },
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
                    modifier = Modifier.animateItemPlacement(),
                )
            }
        }
    }
}
