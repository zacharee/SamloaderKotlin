package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import kotlinx.browser.window

@Composable
internal actual fun getThemeInfo(): ThemeInfo {
    val dark = window.matchMedia("(prefers-color-scheme: dark)").matches

    return ThemeInfo(
        isDarkMode = dark
    )
}
