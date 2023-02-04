package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class ThemeInfo(
    val isDarkMode: Boolean,
    val primaryColor: Color? = null,
    val accentColor: Color? = null,
    val backgroundColor: Color? = null,
    val onPrimaryColor: Color? = null,
    val onSecondaryColor: Color? = null,
    val onBackgroundColor: Color? = null,
)

@Composable
expect fun getThemeInfo(): ThemeInfo
