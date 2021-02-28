import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.utils.io.core.internal.*
import tk.zwander.common.MainView
import tk.zwander.common.model.DecryptModel
import tk.zwander.common.model.DownloadModel
import tk.zwander.common.view.*
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