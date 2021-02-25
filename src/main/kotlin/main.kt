import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.DecryptModel
import model.DownloadModel
import view.DecryptView
import view.DownloadView
import javax.swing.UIManager

enum class Page {
    DOWNLOADER,
    DECRYPTER
}

fun main() = Window {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    var page by remember { mutableStateOf(Page.DOWNLOADER) }

    val downloadModel = remember { DownloadModel() }
    val decryptModel = remember { DecryptModel() }

    MaterialTheme(
        colors = darkColors()
    ) {
        Surface {
            Column(
                modifier = Modifier.fillMaxSize()
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
        }
    }
}