@file:JvmName("ThemeUtilsJVM")

package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.spec.ColorToneRule
import com.jthemedetecor.OsThemeDetector
import korlibs.memory.Platform
import tk.zwander.common.ui.GenericLinuxThemeDetector
import tk.zwander.commonCompose.monet.ColorScheme

@Composable
actual fun getThemeInfo(): ThemeInfo {
    val manager = LafManager.getPreferredThemeStyle()

    val (osThemeDetector, isSupported) = remember {
        OsThemeDetector.getDetector() to OsThemeDetector.isSupported()
    }

    val genericDetector = remember {
        GenericLinuxThemeDetector()
    }

    var dark by remember {
        mutableStateOf(
            when {
                Platform.isLinux -> genericDetector.isDark
                isSupported -> osThemeDetector.isDark
                else -> manager.colorToneRule == ColorToneRule.DARK
            }
        )
    }

    DisposableEffect(osThemeDetector, isSupported, genericDetector) {
        val listener = { darkMode: Boolean ->
            dark = darkMode
        }

        if (Platform.isLinux) {
            genericDetector.registerListener(listener)
        } else if (isSupported) {
            osThemeDetector.registerListener(listener)
        }

        onDispose {
            if (Platform.isLinux) {
                genericDetector.removeListener(listener)
            } else if (isSupported) {
                osThemeDetector.removeListener(listener)
            }
        }
    }

    return ThemeInfo(
        isDarkMode = dark,
        colors = ColorScheme(
            manager.accentColorRule?.accentColor?.rgb
                ?: Color(red = 208, green = 188, blue = 255).toArgb(),
            dark
        ).toComposeColorScheme().toNullableColorScheme(),
    )
}
