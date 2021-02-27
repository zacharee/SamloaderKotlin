import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.io.core.internal.DangerousInternalIoApi
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.view.CustomMaterialTheme
import tk.zwander.common.view.DecryptView
import tk.zwander.common.view.DownloadView
import tk.zwander.common.view.FooterView
import javax.swing.UIManager
import kotlin.time.ExperimentalTime

enum class Page {
    DOWNLOADER,
    DECRYPTER
}

@ExperimentalTime
@OptIn(DangerousInternalIoApi::class)
fun main() = Window(
    title = "Samsung Firmware Downloader",
    icon = getImage("icon.png")
) {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    var page by remember { mutableStateOf(Page.DOWNLOADER) }

    val downloadModel = remember { DownloadModel() }
    val decryptModel = remember { DecryptModel() }

    CustomMaterialTheme {
        Surface {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                        .fillMaxWidth()
                ) {
                    TabRow(
                        modifier = Modifier.fillMaxWidth()
                            .height(48.dp),
                        selectedTabIndex = page.ordinal
                    ) {
                        Tab(
                            selected = page == Page.DOWNLOADER,
                            text = { Text("Downloader") },
                            onClick = {
                                page = Page.DOWNLOADER
                            }
                        )
                        Tab(
                            selected = page == Page.DECRYPTER,
                            text = { Text("Decrypter") },
                            onClick = {
                                page = Page.DECRYPTER
                            }
                        )
                    }

                    Divider(
                        thickness = 1.dp,
                        color = MaterialTheme.colors.onSurface
                    )

                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxSize()
                            .padding(8.dp)
                    ) {
                        when (page) {
                            Page.DOWNLOADER -> DownloadView(downloadModel)
                            Page.DECRYPTER -> DecryptView(decryptModel)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                FooterView()
            }
        }
    }
}