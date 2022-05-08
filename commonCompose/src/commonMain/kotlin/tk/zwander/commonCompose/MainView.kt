package tk.zwander.commonCompose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soywiz.korio.async.launch
import com.soywiz.korio.util.OS
import io.ktor.utils.io.core.internal.*
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.model.HistoryModel
import tk.zwander.commonCompose.util.currentPageEnum
import tk.zwander.commonCompose.util.pager.ExperimentalPagerApi
import tk.zwander.commonCompose.util.pager.HorizontalPager
import tk.zwander.commonCompose.util.pager.rememberPagerState
import tk.zwander.commonCompose.view.components.CustomMaterialTheme
import tk.zwander.commonCompose.view.components.FooterView
import tk.zwander.commonCompose.view.components.Page
import tk.zwander.commonCompose.view.components.TabView
import tk.zwander.commonCompose.view.pages.DecryptView
import tk.zwander.commonCompose.view.pages.DownloadView
import tk.zwander.commonCompose.view.pages.HistoryView
import kotlin.time.ExperimentalTime

val downloadModel = DownloadModel()
val decryptModel = DecryptModel()
val historyModel = HistoryModel()

/**
 * The main UI view.
 */
@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(DangerousInternalIoApi::class, ExperimentalPagerApi::class)
@ExperimentalTime
@Composable
fun MainView(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState(0)
    val pagerState = rememberPagerState(Page.DOWNLOADER.index, !OS.isJvm)
    val scope = rememberCoroutineScope()

    CustomMaterialTheme {
        Surface {
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                fun updateCurrentPage(page: Page) {
                    pagerState.currentPageEnum = page
                    scope.launch {
                        pagerState.animateScrollToPage(page.index)
                    }
                }

                TabView(
                    pagerState = pagerState,
                    selectedPage = pagerState.currentPageEnum,
                    onPageSelected = {
                        updateCurrentPage(it)
                    }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .widthIn(max = 1200.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    val historyDownloadCallback = { model: String, region: String, fw: String ->
                        downloadModel.manual = true
                        downloadModel.osCode = ""
                        downloadModel.model = model
                        downloadModel.region = region
                        downloadModel.fw = fw

                        updateCurrentPage(Page.DOWNLOADER)
                    }

                    val historyDecryptCallback = { model: String, region: String, fw: String ->
                        decryptModel.fileToDecrypt = null
                        decryptModel.model = model
                        decryptModel.region = region
                        decryptModel.fw = fw

                        updateCurrentPage(Page.DECRYPTER)
                    }

                    if (OS.isJvm) {
                        pagerState.overridePageCount = 3

                        Crossfade(
                            targetState = pagerState.currentPageEnum
                        ) {
                            when (it) {
                                Page.DOWNLOADER -> DownloadView(downloadModel, scrollState)
                                Page.DECRYPTER -> DecryptView(decryptModel, scrollState)
                                Page.HISTORY -> HistoryView(
                                    historyModel, historyDownloadCallback,
                                    historyDecryptCallback
                                )
                            }
                        }
                    } else {
                        HorizontalPager(
                            count = 3,
                            state = pagerState,
                            itemSpacing = 8.dp,
                        ) { p ->
                            when (Page.values()[p]) {
                                Page.DOWNLOADER -> DownloadView(downloadModel, scrollState)
                                Page.DECRYPTER -> DecryptView(decryptModel, scrollState)
                                Page.HISTORY -> HistoryView(
                                    historyModel, historyDownloadCallback,
                                    historyDecryptCallback
                                )
                            }
                        }
                    }
                }

                FooterView()
            }
        }
    }
}