import androidx.compose.desktop.Window
import io.ktor.utils.io.core.internal.*
import tk.zwander.common.MainView
import javax.swing.UIManager
import kotlin.time.ExperimentalTime

@ExperimentalTime
@OptIn(DangerousInternalIoApi::class)
fun main() = Window(
    title = "Samsung Firmware Downloader",
    icon = getImage("icon.png")
) {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    MainView()
}