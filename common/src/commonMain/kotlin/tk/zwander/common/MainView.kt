package tk.zwander.common

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.utils.io.core.internal.*
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.model.HistoryModel
import tk.zwander.common.view.*
import tk.zwander.common.view.pages.DecryptView
import tk.zwander.common.view.pages.DownloadView
import tk.zwander.common.view.pages.HistoryView
import kotlin.time.ExperimentalTime

@OptIn(DangerousInternalIoApi::class)
@ExperimentalTime
@Composable
fun MainView() {
    val page = remember { mutableStateOf(Page.DOWNLOADER) }

    val downloadModel = remember { DownloadModel() }
    val decryptModel = remember { DecryptModel() }
    val historyModel = remember { HistoryModel() }

    val scrollState = rememberScrollState(0)

    CustomMaterialTheme {
        Surface {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TabView(page)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    Crossfade(
                        targetState = page.value,
                        animationSpec = tween(300)
                    ) {
                        when (it) {
                            Page.DOWNLOADER -> DownloadView(downloadModel, scrollState)
                            Page.DECRYPTER -> DecryptView(decryptModel, scrollState)
                            Page.HISTORY -> HistoryView(
                                historyModel,
                                { model, region, fw ->
                                    downloadModel.manual = true
                                    downloadModel.model = model
                                    downloadModel.region = region
                                    downloadModel.fw = fw

                                    page.value = Page.DOWNLOADER
                                },
                                { model, region, fw ->
                                    decryptModel.fileToDecrypt = null
                                    decryptModel.model = model
                                    decryptModel.region = region
                                    decryptModel.fw = fw

                                    page.value = Page.DECRYPTER
                                }
                            )
                        }
                    }
                }

                FooterView()
            }
        }
    }
}