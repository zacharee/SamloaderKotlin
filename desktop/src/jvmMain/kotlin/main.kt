import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.*
import io.ktor.utils.io.core.internal.*
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import tk.zwander.common.data.appName
import tk.zwander.common.util.UrlHandler
import tk.zwander.commonCompose.MainView
import java.awt.Desktop
import javax.swing.*
import kotlin.time.ExperimentalTime

@ExperimentalTime
@OptIn(DangerousInternalIoApi::class, ExperimentalComposeUiApi::class)
fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("apple.awt.application.appearance", "system")
    System.setProperty("apple.awt.application.name", appName)

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

//                        Divider()

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
