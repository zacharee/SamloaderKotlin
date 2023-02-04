package tk.zwander.commonCompose.view.components

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import tk.zwander.common.data.*
import tk.zwander.commonCompose.util.ThemeInfo
import tk.zwander.commonCompose.util.getThemeInfo

/**
 * A Material theme with custom colors and such.
 */
@Composable
fun CustomMaterialTheme(block: @Composable()() -> Unit) {
    val themeInfo = getThemeInfo()

    val colorScheme = if (themeInfo.isDarkMode) {
        darkColorScheme().setColors(themeInfo)
    } else {
        lightColorScheme().setColors(themeInfo)
    }

    MaterialTheme(
        colorScheme = colorScheme,
    ) {
        block()
    }
}

@Composable
private fun ColorScheme.setColors(themeInfo: ThemeInfo): ColorScheme {
    val base = copy(
        primary = themeInfo.primaryColor ?: primary,
        secondary = themeInfo.accentColor ?: secondary,
        background = themeInfo.backgroundColor ?: background
    )

    val onPrimary = themeInfo.onPrimaryColor ?: Color.White
    val onSecondary = themeInfo.onSecondaryColor ?: Color.White
    val onBackground = themeInfo.onBackgroundColor ?: base.onBackground

    return base.copy(
        onPrimary = onPrimary,
        onSecondary = onSecondary,
        onBackground = onBackground,
    )
}
