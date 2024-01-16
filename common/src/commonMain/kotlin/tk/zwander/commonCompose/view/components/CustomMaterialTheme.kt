package tk.zwander.commonCompose.view.components

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import tk.zwander.commonCompose.util.FontMapper
import tk.zwander.commonCompose.util.ThemeInfo
import tk.zwander.commonCompose.util.getThemeInfo

/**
 * A Material theme with custom colors and such.
 */
@Composable
fun CustomMaterialTheme(block: @Composable () -> Unit) {
    val themeInfo = getThemeInfo()

    val colorScheme = if (themeInfo.isDarkMode) {
        darkColorScheme().setColors(themeInfo)
    } else {
        lightColorScheme().setColors(themeInfo)
    }

    MaterialTheme(
        colorScheme = colorScheme,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(
                fontFamily = LocalTextStyle.current.fontFamily?.let {
                    FontMapper.mapGenericFontFamilyToSpecificFontFamily(it)
                },
            ),
        ) {
            block()
        }
    }
}

@Composable
private fun ColorScheme.setColors(themeInfo: ThemeInfo): ColorScheme {
    val base = themeInfo.colors?.mergeWithColorScheme(this) ?: this

    val onPrimary = themeInfo.colors?.onPrimary ?: Color.White
    val onSecondary = themeInfo.colors?.onSecondary ?: Color.White
    val onBackground = themeInfo.colors?.onBackground ?: base.onBackground
    val onSurface = themeInfo.colors?.onSurface ?: base.onSurface

    return base.copy(
        onPrimary = onPrimary,
        onSecondary = onSecondary,
        onBackground = onBackground,
        onSurface = onSurface,
    )
}
