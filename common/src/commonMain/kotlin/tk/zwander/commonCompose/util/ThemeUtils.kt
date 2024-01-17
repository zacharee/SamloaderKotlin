package tk.zwander.commonCompose.util

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

data class ThemeInfo(
    val isDarkMode: Boolean,
    val colors: ColorScheme,
)

@Composable
expect fun getThemeInfo(): ThemeInfo
