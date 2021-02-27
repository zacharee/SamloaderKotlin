import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.view.DecryptView
import tk.zwander.common.view.DownloadView
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.UIManager
import kotlin.time.ExperimentalTime

enum class Page {
    DOWNLOADER,
    DECRYPTER
}

@ExperimentalTime
fun main() = Window(
    title = "Samsung Firmware Downloader",
    icon = Thread.currentThread().contextClassLoader.getResource("icon.png")
        ?.openStream().use(ImageIO::read)
) {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    Test.main(arrayOf())

    var page by remember { mutableStateOf(Page.DOWNLOADER) }

    val downloadModel = remember { DownloadModel() }
    val decryptModel = remember { DecryptModel() }

    MaterialTheme(
        colors = darkColors(
            primary = Color(0x4C, 0xAF, 0x50),
            primaryVariant = Color(0x52, 0xC7, 0x56),
            secondary = Color(0xFFBB86FC),
            secondaryVariant = Color(0xFF3700B3)
        )
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