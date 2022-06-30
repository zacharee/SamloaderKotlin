import androidx.compose.ui.window.Window
import platform.AppKit.NSApp
import platform.AppKit.NSApplication
import tk.zwander.commonCompose.MainView
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun main() {
    NSApplication.sharedApplication()
    Window("Test") {
        MainView()
    }
    NSApp?.run()
}
