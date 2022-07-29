package tk.zwander.commonCompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Surface
import androidx.compose.material.TabPosition
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.utils.io.core.internal.*
import tk.zwander.commonCompose.locals.LocalDecryptModel
import tk.zwander.commonCompose.locals.LocalDownloadModel
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
fun MainView(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState(0)
    var currentPage by remember { mutableStateOf(Page.DOWNLOADER) }
    var indicator by remember { mutableStateOf<(@Composable() (List<TabPosition>) -> Unit)?>(null) }

    ProvideModels {
        val downloadModel = LocalDownloadModel.current
        val decryptModel = LocalDecryptModel.current

        CustomMaterialTheme {
            Surface {
                Column(
                    modifier = modifier.fillMaxSize()
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

                            currentPage = Page.DOWNLOADER
                        }

                        val historyDecryptCallback = { model: String, region: String, fw: String ->
                            decryptModel.fileToDecrypt = null
                            decryptModel.model = model
                            decryptModel.region = region
                            decryptModel.fw = fw

                            currentPage = Page.DECRYPTER
                        }

                        indicator = HorizontalPager(
                            count = 3,
                            currentPage = currentPage.index,
                            onPageChanged = { currentPage = Page.values()[it] },
                            eval = {
                                when (it) {
                                    Page.DOWNLOADER -> DownloadView(scrollState)
                                    Page.DECRYPTER -> DecryptView(scrollState)
                                    Page.HISTORY -> HistoryView(
                                        historyDownloadCallback,
                                        historyDecryptCallback
                                    )
                                }
                            }
                        )
                    }

                    FooterView()
                }
            }
        }
    }
}
