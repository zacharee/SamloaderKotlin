import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.bugsnag.Bugsnag
import com.formdev.flatlaf.FlatDarkLaf
import korlibs.memory.Platform
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import tk.zwander.common.GradleConfig
import tk.zwander.common.MainBase
import tk.zwander.common.util.UrlHandler
import tk.zwander.common.util.invoke
import tk.zwander.commonCompose.MainView
import tk.zwander.commonCompose.util.FilePicker
import tk.zwander.samloaderkotlin.resources.MR
import java.awt.Desktop
import java.awt.Dimension
import javax.swing.*
import kotlin.time.ExperimentalTime


@ExperimentalTime
fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.appearance", "system")
    System.setProperty("apple.awt.application.name", GradleConfig.appName)

    // Some GPUs don't like Direct3D
    if (Platform.isWindows) {
        System.setProperty("skiko.renderApi", "OPENGL")
    }

    val bugsnag = Bugsnag("a5b9774e86bc615c2e49a572b8313489")
    bugsnag.setAppVersion(GradleConfig.versionName)
    bugsnag.addCallback {
        it.addToTab("app", "version_code", GradleConfig.versionCode)
        it.addToTab("app", "jdk_architecture", System.getProperty("sun.arch.data.model"))
    }

    MainBase()

    application {
        var aboutState by remember { mutableStateOf(false) }
        var showingSupportersWindow by remember { mutableStateOf(false) }
        val mainWindowState = rememberWindowState()

        Desktop.getDesktop().apply {
            if (isSupported(Desktop.Action.APP_ABOUT)) {
                setAboutHandler {
                    aboutState = true
                }
            }
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Bifrost",
            icon = getImage("icon.png").toPainter(),
            state = mainWindowState
        ) {
            with (LocalDensity.current) {
                val minWidth = 200.dp
                val minHeight = 250.dp
                window.minimumSize = Dimension(minWidth.roundToPx(), minHeight.roundToPx())
            }

            UIManager.setLookAndFeel(FlatDarkLaf())
            FilePicker.init(window)

            if (hostOs == OS.MacOS) {
                MenuBar {
                    Menu(
                        text = MR.strings.window()
                    ) {
                        Item(
                            text = MR.strings.minimize(),
                            onClick = {
                                mainWindowState.isMinimized = true
                            },
                            shortcut = KeyShortcut(Key.M, meta = true)
                        )

                        Item(
                            text = MR.strings.zoom(),
                            onClick = {
                                mainWindowState.placement = WindowPlacement.Maximized
                            }
                        )

                        Item(
                            text = MR.strings.close(),
                            onClick = {
                                exitApplication()
                            },
                            shortcut = KeyShortcut(Key.W, meta = true)
                        )
                    }

                    Menu(
                        text = MR.strings.help()
                    ) {
                        Item(
                            text = MR.strings.github(),
                            onClick = {
                                UrlHandler.launchUrl("https://github.com/zacharee/SamloaderKotlin")
                            }
                        )

                        Item(
                            text = MR.strings.mastodon(),
                            onClick = {
                                UrlHandler.launchUrl("https://androiddev.social/@wander1236")
                            }
                        )

                        Item(
                            text = MR.strings.twitter(),
                            onClick = {
                                UrlHandler.launchUrl("https://twitter.com/wander1236")
                            }
                        )

                        Item(
                            text = MR.strings.patreon(),
                            onClick = {
                                UrlHandler.launchUrl("https://patreon.com/zacharywander")
                            }
                        )

                        Item(
                            text = MR.strings.supporters(),
                            onClick = {
                                showingSupportersWindow = true
                            }
                        )
                    }
                }
            }

            MainView()
        }

        AboutDialog(
            aboutState
        ) { aboutState = false }

        PatreonSupportersWindow(
            showingSupportersWindow
        ) {
            showingSupportersWindow = false
        }
    }
}
