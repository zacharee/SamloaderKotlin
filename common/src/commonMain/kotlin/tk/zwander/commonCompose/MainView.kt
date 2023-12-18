package tk.zwander.commonCompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.TabPosition
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.utils.io.core.internal.*
import korlibs.io.async.launch
import tk.zwander.commonCompose.locals.LocalDecryptModel
import tk.zwander.commonCompose.locals.LocalDownloadModel
import tk.zwander.commonCompose.locals.LocalHistoryModel
import tk.zwander.commonCompose.locals.ProvideModels
import tk.zwander.commonCompose.view.pager.HorizontalPager
import tk.zwander.commonCompose.view.components.CustomMaterialTheme
import tk.zwander.commonCompose.view.components.FooterView
import tk.zwander.commonCompose.view.components.Page
import tk.zwander.commonCompose.view.components.TabView
import tk.zwander.commonCompose.view.pages.DecryptView
import tk.zwander.commonCompose.view.pages.DownloadView
import tk.zwander.commonCompose.view.pages.HistoryView
import kotlin.time.ExperimentalTime

/**
 * The main UI view.
 */
@OptIn(DangerousInternalIoApi::class)
@ExperimentalTime
@Composable
fun MainView(
    modifier: Modifier = Modifier,
    fullPadding: PaddingValues = PaddingValues(),
) {
    val scope = rememberCoroutineScope()

    var currentPage by remember { mutableStateOf(Page.DOWNLOADER) }
    var indicator by remember { mutableStateOf<(@Composable (List<TabPosition>) -> Unit)?>(null) }

    ProvideModels {
        val downloadModel = LocalDownloadModel.current
        val decryptModel = LocalDecryptModel.current
        val historyModel = LocalHistoryModel.current

        scope.launch {
            downloadModel.onCreate()
        }

        scope.launch {
            decryptModel.onCreate()
        }

        scope.launch {
            historyModel.onCreate()
        }

        CustomMaterialTheme {
            Surface {
                Column(
                    modifier = modifier.fillMaxSize()
                        .padding(fullPadding),
                ) {
                    TabView(
                        selectedPage = currentPage,
                        onPageSelected = {
                            currentPage = it
                        },
                        indicator = indicator
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = 1200.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        val historyDownloadCallback = { model: String, region: String, fw: String ->
                            downloadModel.manual.value = true
                            downloadModel.osCode.value = ""
                            downloadModel.model.value = model
                            downloadModel.region.value = region
                            downloadModel.fw.value = fw

                            currentPage = Page.DOWNLOADER
                        }

                        val historyDecryptCallback = { model: String, region: String, fw: String ->
                            decryptModel.fileToDecrypt.value = null
                            decryptModel.model.value = model
                            decryptModel.region.value = region
                            decryptModel.fw.value = fw

                            currentPage = Page.DECRYPTER
                        }

                        indicator = HorizontalPager(
                            count = 3,
                            currentPage = currentPage.index,
                            onPageChanged = { currentPage = Page.entries[it] },
                            eval = {
                                when (it) {
                                    Page.DOWNLOADER -> DownloadView()
                                    Page.DECRYPTER -> DecryptView()
                                    Page.HISTORY -> HistoryView(
                                        historyDownloadCallback,
                                        historyDecryptCallback,
                                    )
                                }
                            },
                        )
                    }

                    FooterView()
                }
            }
        }
    }
}
