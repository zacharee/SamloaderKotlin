import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.*
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import tk.zwander.common.GradleConfig
import tk.zwander.common.util.UrlHandler
import tk.zwander.commonCompose.MainView
import tk.zwander.commonCompose.view.pages.PlatformDownloadView
import java.awt.Desktop
import javax.swing.*
import kotlin.time.ExperimentalTime

@Suppress("OPT_IN_IS_NOT_ENABLED")
@ExperimentalTime
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.appearance", "system")
    System.setProperty("apple.awt.application.name", GradleConfig.appName)

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
            PlatformDownloadView.setWindow(window)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

            if (hostOs == OS.MacOS) {
                MenuBar {
                    Menu(
                        text = "Window"
                    ) {
                        Item(
                            text = "Minimize",
                            onClick = {
                                mainWindowState.isMinimized = true
                            },
                            shortcut = KeyShortcut(Key.M, meta = true)
                        )

                        Item(
                            text = "Zoom",
                            onClick = {
                                mainWindowState.placement = WindowPlacement.Maximized
                            }
                        )

                        Item(
                            text = "Close",
                            onClick = {
                                exitApplication()
                            },
                            shortcut = KeyShortcut(Key.W, meta = true)
                        )
                    }

                    Menu(
                        text = "Help"
                    ) {
                        Item(
                            text = "Check for Updates...",
                            onClick = {

                            }
                        )

                        Separator()

                        Item(
                            text = "GitHub",
                            onClick = {
                                UrlHandler.launchUrl("https://github.com/zacharee/SamloaderKotlin")
                            }
                        )

                        Item(
                            text = "Twitter",
                            onClick = {
                                UrlHandler.launchUrl("https://twitter.com/wander1236")
                            }
                        )

                        Item(
                            text = "Patreon",
                            onClick = {
                                UrlHandler.launchUrl("https://patreon.com/zacharywander")
                            }
                        )

                        Item(
                            text = "Supporters",
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
