package tk.zwander.commonCompose.util

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ThemeInfo(
    val isDarkMode: Boolean,
    val colors: ColorScheme,
)

@Composable
expect fun getThemeInfo(): ThemeInfo

object ThemeConstants {
    object Colors {
        val scrollbarUnselected: Color
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

        val scrollbarSelected: Color
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    }

    object Dimensions {
        val scrollbarPadding: Dp
            @Composable
            @ReadOnlyComposable
            get() = 1.dp

        val scrollbarThickness: Dp
            @Composable
            @ReadOnlyComposable
            get() = 4.dp
    }
}
