@file:JvmName("ThemeUtilsJVM")

package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.info.ColorToneRule

@Composable
internal actual fun getThemeInfo(): ThemeInfo {
    val manager = LafManager.getPreferredThemeStyle()
    val dark = manager.colorToneRule == ColorToneRule.DARK

    return ThemeInfo(
        isDarkMode = dark,
        colors = NullableColorScheme(
            primary = manager.accentColorRule.accentColor.let {
                Color(it.red, it.green, it.blue, it.alpha)
            },
            secondary = manager.accentColorRule.selectionColor.let {
                Color(it.red, it.green, it.blue, it.alpha)
            },
        ),
    )
}
