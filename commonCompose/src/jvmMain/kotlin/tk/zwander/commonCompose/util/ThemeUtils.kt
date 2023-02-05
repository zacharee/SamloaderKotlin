@file:JvmName("ThemeUtilsJVM")

package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.info.ColorToneRule
import tk.zwander.commonCompose.monet.ColorScheme

@Composable
internal actual fun getThemeInfo(): ThemeInfo {
    val manager = LafManager.getPreferredThemeStyle()
    val dark = manager.colorToneRule == ColorToneRule.DARK

    return ThemeInfo(
        isDarkMode = dark,
        colors = ColorScheme(
            manager.accentColorRule.accentColor.rgb,
            dark
        ).toComposeColorScheme().toNullableColorScheme(),
    )
}
