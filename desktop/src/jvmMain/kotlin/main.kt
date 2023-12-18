import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import io.github.mimoguz.customwindow.DwmAttribute
import io.github.mimoguz.customwindow.WindowHandle
import korlibs.memory.Platform
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import tk.zwander.common.GradleConfig
import tk.zwander.common.MainBase
import tk.zwander.common.util.BifrostSettings
import tk.zwander.common.util.BugsnagUtils
import tk.zwander.common.util.UrlHandler
import tk.zwander.common.util.invoke
import tk.zwander.commonCompose.MainView
import tk.zwander.commonCompose.util.FilePicker
import tk.zwander.commonCompose.util.getThemeInfo
import tk.zwander.samloaderkotlin.resources.MR
import java.awt.Desktop
import java.awt.Dimension
import java.util.UUID
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

    val uuid = BifrostSettings.settings.getStringOrNull(BugsnagUtils.UUID_KEY) ?: UUID.randomUUID().toString().also {
        BifrostSettings.settings.putString(BugsnagUtils.UUID_KEY, it)
    }

    val bugsnag = BugsnagUtils.bugsnag
    bugsnag.setAppVersion(GradleConfig.versionName)
    bugsnag.addCallback {
        it.setUserId(uuid)
        it.addToTab("app", "version_code", GradleConfig.versionCode)
        it.addToTab("app", "jdk_architecture", System.getProperty("sun.arch.data.model"))
    }
    bugsnag.setAutoCaptureSessions(true)

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
            title = GradleConfig.appName,
            icon = getImage("icon.png")?.toPainter(),
            state = mainWindowState
        ) {
            // For some reason this returns the title bar height on macOS.
            val menuBarHeight = remember {
                if (hostOs == OS.MacOS) window.height.dp else 0.dp
            }

            val density = LocalDensity.current

            LaunchedEffect(null) {
                // Set this after getting the original height.
                window.minimumSize = with(density) {
                    Dimension(200.dp.roundToPx(), 200.dp.roundToPx())
                }

                window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
            }

            FilePicker.init(window)

            when (hostOs) {
                OS.MacOS -> {
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
                OS.Windows -> {
                    val themeInfo = getThemeInfo()

                    LaunchedEffect(themeInfo) {
                        try {
                            val handle = WindowHandle.tryFind(window)

                            handle.dwmSetBooleanValue(DwmAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE, true)

                            themeInfo.colors?.background?.let {
                                handle.setCaptionColor(it)
                            }

                            themeInfo.colors?.primary?.let {
                                handle.setBorderColor(it)
                            }

                            themeInfo.colors?.onBackground?.let {
                                handle.setTextColor(it)
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }
                else -> {}
            }

            MainView(
                fullPadding = PaddingValues(top = menuBarHeight),
            )
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
