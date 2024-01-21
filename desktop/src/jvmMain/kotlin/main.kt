import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLaf
import com.mayakapps.compose.windowstyler.NativeLookWindow
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowFrameStyle
import com.sun.jna.ptr.IntByReference
import korlibs.memory.Platform
import tk.zwander.common.GradleConfig
import tk.zwander.common.MainBase
import tk.zwander.common.util.BifrostSettings
import tk.zwander.common.util.BugsnagUtils
import tk.zwander.common.util.isWindows11
import tk.zwander.common.util.jna.windows.DwmImpl
import tk.zwander.common.util.jna.windows.DwmWindowAttribute
import tk.zwander.common.util.jna.windows.hwnd
import tk.zwander.common.util.jna.windows.toBgr
import tk.zwander.commonCompose.MainView
import tk.zwander.commonCompose.util.FilePicker
import tk.zwander.commonCompose.util.getThemeInfo
import tk.zwander.commonCompose.view.LocalMenuBarHeight
import tk.zwander.commonCompose.view.MacMenuBar
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

    val uuid = BifrostSettings.settings.getStringOrNull(BugsnagUtils.UUID_KEY) ?: UUID.randomUUID()
        .toString().also {
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
        val mainWindowState = rememberWindowState()
        val themeInfo = getThemeInfo()
        val density = LocalDensity.current
        val useMicaEffect by BifrostSettings.Keys.useMicaEffect.collectAsMutableState()

        val captionColor =
            if (useMicaEffect == true) Color.Unspecified else themeInfo.colors.onBackground
        val titleBarColor =
            if (useMicaEffect == true) Color.Unspecified else themeInfo.colors.background

        NativeLookWindow(
            onCloseRequest = ::exitApplication,
            title = GradleConfig.appName,
            icon = getImage("icon.png")?.toPainter(),
            state = mainWindowState,
            preferredBackdropType = WindowBackdrop.MicaTabbed(themeInfo.isDarkMode),
            frameStyle = WindowFrameStyle(
                borderColor = themeInfo.colors.background,
                captionColor = captionColor,
                titleBarColor = titleBarColor,
            ),
        ) {
            // For some reason this returns the title bar height on macOS.
            val menuBarHeight = remember(window.height) {
                if (Platform.isMac) window.height.dp else 0.dp
            }

//            LaunchedEffect(null) {
//                withContext(Dispatchers.IO) {
//                    SwingUtilities.invokeAndWait {
//                        if (!window.isTransparent) {
//                            window.setComposeLayerTransparency(true)
//                            window.hackContentPane()
//                        }
//                    }
//                }
//            }

//            LaunchedEffect(window.isVisible) {
//                delay(TimeSpan(1000.0))
//                AppKit.NSWindow.getWindow()
//            }

            LaunchedEffect(window) {
                window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)

                FilePicker.init(window)

                val map = mutableMapOf<String, String>()

                themeInfo.colors.primary.toArgb().let {
                    map.put(
                        "@accentColor",
                        String.format("#%06x", (java.awt.Color(it, true).rgb and 0xffffff)),
                    )
                }
                themeInfo.colors.background.toArgb().let {
                    map.put(
                        "@background",
                        String.format("#%06x", (java.awt.Color(it, true).rgb and 0xffffff)),
                    )
                }

                FlatLaf.setGlobalExtraDefaults(map)
                FlatDarkLaf.setup()
            }

            LaunchedEffect(density) {
                // Set this after getting the original height.
                window.minimumSize = with(density) {
                    Dimension(200.dp.roundToPx(), 200.dp.roundToPx())
                }
            }

            LaunchedEffect(captionColor, titleBarColor) {
                if (isWindows11) {
                    DwmImpl.DwmSetWindowAttribute(
                        hwnd = window.hwnd,
                        attribute = DwmWindowAttribute.DWMWA_TEXT_COLOR.value,
                        value = IntByReference(captionColor.toBgr()),
                        valueSize = 4,
                    )
                    DwmImpl.DwmSetWindowAttribute(
                        hwnd = window.hwnd,
                        attribute = DwmWindowAttribute.DWMWA_CAPTION_COLOR.value,
                        value = IntByReference(titleBarColor.toBgr()),
                        valueSize = 4,
                    )
                }
            }

            MacMenuBar(
                mainWindowState = mainWindowState,
                applicationScope = this@application,
            )

            CompositionLocalProvider(
                LocalMenuBarHeight provides menuBarHeight,
            ) {
                MainView(
                    fullPadding = PaddingValues(top = LocalMenuBarHeight.current),
                )
            }
        }
    }
}
