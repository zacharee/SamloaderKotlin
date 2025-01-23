package tk.zwander.commonCompose.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.ScrollbarSelectionMode
import my.nanihadesuka.compose.ScrollbarSettings

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

    object ScrollBarSettings {
        val Default: ScrollbarSettings
            @Composable
            @ReadOnlyComposable
            get() = ScrollbarSettings.Default.copy(
                thumbUnselectedColor = Colors.scrollbarUnselected,
                thumbSelectedColor = Colors.scrollbarSelected,
                alwaysShowScrollbar = false,
                scrollbarPadding = Dimensions.scrollbarPadding,
                thumbThickness = Dimensions.scrollbarThickness,
                selectionMode = ScrollbarSelectionMode.Disabled,
            )
    }
}
