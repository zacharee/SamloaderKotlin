import androidx.compose.ui.window.Window
import platform.AppKit.NSApp
import platform.AppKit.NSApplication
import tk.zwander.common.GradleConfig
import tk.zwander.commonCompose.MainView
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    NSApplication.sharedApplication()
    Window(GradleConfig.appName) {
        MainView()
    }
    NSApp?.run()
}
