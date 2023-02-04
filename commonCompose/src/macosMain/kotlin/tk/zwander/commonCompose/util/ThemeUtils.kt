package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.useContents
import platform.AppKit.NSColorList
import platform.AppKit.NSColorSpace
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUserDefaults

@Composable
actual fun getThemeInfo(): ThemeInfo {
    val defaults = NSUserDefaults.standardUserDefaults()
    val osVersion = NSProcessInfo.processInfo().operatingSystemVersion()
    val switchesAutomatically = if (osVersion.useContents { this.majorVersion >= 11 || (this.majorVersion >= 10 && this.minorVersion >= 15) }) {
        defaults.boolForKey("AppleInterfaceStyleSwitchesAutomatically")
    } else {
        false
    }
    val darkString = defaults.stringForKey("AppleInterfaceStyle")

    val dark = when {
        switchesAutomatically -> {
            darkString == null
        }

        else -> {
            darkString == "Dark"
        }
    }

    val accentInt = defaults.objectForKey("AppleAccentColor")?.let {
        defaults.integerForKey("AppleAccentColor")
    }

    val accent = accentInt?.let {
        when (accentInt.toInt()) {
            -2 -> ACCENT_BLUE
            -1 -> ACCENT_GRAPHITE
            0 -> ACCENT_RED
            1 -> ACCENT_ORANGE
            2 -> ACCENT_YELLOW
            3 -> ACCENT_GREEN
            4 -> ACCENT_LILAC
            5 -> ACCENT_ROSE
            else -> null
        }
    }

    val selectionColor = run {
        val color = NSColorList.colorListNamed("System")
            ?.colorWithKey("selectedTextBackgroundColor")
            ?.colorUsingColorSpace(NSColorSpace.sRGBColorSpace())

        color?.let {
            Color(
                red = (255 * color.redComponent + 0.5).toInt(),
                green = (255 * color.greenComponent + 0.5).toInt(),
                blue = (255 * color.blueComponent + 0.5).toInt(),
                alpha = (255 * color.alphaComponent + 0.5).toInt(),
            )
        }
    }

    return ThemeInfo(
        isDarkMode = dark,
        colors = NullableColorScheme(
            primary = accent,
            secondary = selectionColor,
        ),
    )
}
