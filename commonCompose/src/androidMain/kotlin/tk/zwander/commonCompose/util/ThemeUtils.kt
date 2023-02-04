package tk.zwander.commonCompose.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getThemeInfo(): ThemeInfo {
    val context = LocalContext.current

    val dark = isSystemInDarkTheme()
    val colorScheme = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

    return ThemeInfo(
        isDarkMode = isSystemInDarkTheme(),
        primaryColor = colorScheme.primary,
        accentColor = colorScheme.secondary,
        backgroundColor = colorScheme.background,
        onPrimaryColor = colorScheme.onPrimary,
        onSecondaryColor = colorScheme.onSecondary,
        onBackgroundColor = colorScheme.onBackground,
    )
}
