import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import io.ktor.utils.io.core.internal.*
import tk.zwander.commonCompose.MainView
import tk.zwander.commonCompose.util.imageResource
import tk.zwander.commonCompose.util.vectorResource
import tk.zwander.commonCompose.view.components.CustomMaterialTheme
import tk.zwander.commonCompose.view.components.FooterView
import java.awt.Desktop
import javax.swing.*
import kotlin.time.ExperimentalTime

@ExperimentalTime
@OptIn(DangerousInternalIoApi::class)
fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("apple.awt.application.appearance", "system")
    System.setProperty("apple.awt.application.name", "Samsung Firmware Downloader")

    application {
        var aboutState by remember { mutableStateOf(false) }

        Desktop.getDesktop().setAboutHandler {
            aboutState = true
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Samsung Firmware Downloader",
            icon = getImage("icon.png").toPainter()
        ) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

            MainView()
        }

        AboutDialog(
            aboutState
        ) { aboutState = false }
    }
}
