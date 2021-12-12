package tk.zwander.commonCompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soywiz.korio.async.launch
import dev.chrisbanes.snapper.*
import io.ktor.utils.io.core.internal.*
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.model.HistoryModel
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
@OptIn(DangerousInternalIoApi::class, ExperimentalPagerApi::class, ExperimentalSnapperApi::class,
    ExperimentalComposeUiApi::class
)
@ExperimentalTime
@Composable
fun MainView() {
    val scrollState = rememberScrollState(0)

    @Composable
    fun contents(
        page: Page,
        showAll: Boolean = false,
        onPageChange: (Page) -> Unit
    ) {
        val historyDownloadCallback = { model: String, region: String, fw: String ->
            downloadModel.manual = true
            downloadModel.osCode = ""
            downloadModel.model = model
            downloadModel.region = region
            downloadModel.fw = fw

            onPageChange(Page.DOWNLOADER)
        }

        val historyDecryptCallback = { model: String, region: String, fw: String ->
            decryptModel.fileToDecrypt = null
            decryptModel.model = model
            decryptModel.region = region
            decryptModel.fw = fw

            onPageChange(Page.DECRYPTER)
        }

        if (!showAll) {
            when (page) {
                Page.DOWNLOADER -> DownloadView(downloadModel, scrollState)
                Page.DECRYPTER -> DecryptView(decryptModel, scrollState)
                Page.HISTORY -> HistoryView(
                    historyModel, historyDownloadCallback,
                    historyDecryptCallback
                )
            }
        } else {
            DownloadView(downloadModel, scrollState)

            DecryptView(decryptModel, scrollState)

            HistoryView(
                historyModel, historyDownloadCallback,
                historyDecryptCallback
            )
        }
    }

    CustomMaterialTheme {
        Surface {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                val pagerState = rememberPagerState(Page.DOWNLOADER.index)
                val scope = rememberCoroutineScope()

                val currentPage by derivedStateOf {
                    Page.values()[pagerState.currentPage]
                }

                fun currentPage(page: Page) {
                    pagerState.currentPage = page.index
                }

                TabView(
                    pagerState = pagerState,
                    selectedPage = currentPage,
                    onPageSelected = {
                        currentPage(it)
                        scope.launch {
                            pagerState.animateScrollToPage(it.index)
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .widthIn(max = 1200.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    HorizontalPager(
                        count = 3,
                        state = pagerState,
                        itemSpacing = 8.dp,
                    ) { p ->
                        contents(
                            page = Page.values()[p]
                        ) {
                            currentPage(it)
                        }
                    }
                }

                FooterView()
            }
        }
    }
}