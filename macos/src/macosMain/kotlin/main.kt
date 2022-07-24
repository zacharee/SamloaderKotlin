import kotlinx.cinterop.staticCFunction
import moe.tlaster.precompose.PreComposeWindow
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.AppKit.NSApplicationDelegateProtocol
import platform.darwin.NSObject
import platform.objc.objc_setUncaughtExceptionHandler
import tk.zwander.common.GradleConfig
import tk.zwander.commonCompose.MainView
import kotlin.time.ExperimentalTime

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    setUnhandledExceptionHook {
        it.printStackTrace()
    }

    objc_setUncaughtExceptionHandler(staticCFunction<Any?, Unit> {
        println("Error $it")
    })

    val app = NSApplication.sharedApplication()
    app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)

    app.delegate = object : NSObject(), NSApplicationDelegateProtocol {
        override fun applicationShouldTerminateAfterLastWindowClosed(sender: NSApplication): Boolean {
            return true
        }
    }

    PreComposeWindow(GradleConfig.appName) {
        MainView()
    }

    app.run()
}
