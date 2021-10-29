import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.utils.io.core.internal.*
import tk.zwander.commonCompose.MainView
import javax.swing.UIManager
import kotlin.time.ExperimentalTime

@ExperimentalTime
@OptIn(DangerousInternalIoApi::class)
fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("apple.awt.application.appearance", "system")
    System.setProperty("apple.awt.application.name", "Samsung Firmware Downloader");

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Samsung Firmware Downloader",
            icon = getImage("icon.png").toPainter()
        ) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

            MainView()
        }
    }
}
