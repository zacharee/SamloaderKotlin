package tk.zwander.commonCompose.util

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class ThemeInfo(
    val isDarkMode: Boolean,
    val colors: NullableColorScheme? = null,
)

data class NullableColorScheme(
    val primary: Color? = null,
    val onPrimary: Color? = null,
    val primaryContainer: Color? = null,
    val onPrimaryContainer: Color? = null,
    val inversePrimary: Color? = null,
    val secondary: Color? = null,
    val onSecondary: Color? = null,
    val secondaryContainer: Color? = null,
    val onSecondaryContainer: Color? = null,
    val tertiary: Color? = null,
    val onTertiary: Color? = null,
    val tertiaryContainer: Color? = null,
    val onTertiaryContainer: Color? = null,
    val background: Color? = null,
    val onBackground: Color? = null,
    val surface: Color? = null,
    val onSurface: Color? = null,
    val surfaceVariant: Color? = null,
    val onSurfaceVariant: Color? = null,
    val surfaceTint: Color? = null,
    val inverseSurface: Color? = null,
    val inverseOnSurface: Color? = null,
    val error: Color? = null,
    val onError: Color? = null,
    val errorContainer: Color? = null,
    val onErrorContainer: Color? = null,
    val outline: Color? = null,
    val outlineVariant: Color? = null,
    val scrim: Color? = null,
) {
    fun mergeWithColorScheme(scheme: ColorScheme): ColorScheme {
        return ColorScheme(
            primary = primary ?: scheme.primary,
            onPrimary = onPrimary ?: scheme.onPrimary,
            primaryContainer = primaryContainer ?: scheme.primaryContainer,
            onPrimaryContainer = onPrimaryContainer ?: scheme.onPrimaryContainer,
            inversePrimary = inversePrimary ?: scheme.inversePrimary,
            secondary = secondary ?: scheme.secondary,
            onSecondary = onSecondary ?: scheme.onSecondary,
            secondaryContainer = secondaryContainer ?: scheme.secondaryContainer,
            onSecondaryContainer = onSecondaryContainer ?: scheme.onSecondaryContainer,
            tertiary = tertiary ?: scheme.tertiary,
            onTertiary = onTertiary ?: scheme.onTertiary,
            tertiaryContainer = tertiaryContainer ?: scheme.tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer ?: scheme.onTertiaryContainer,
            background = background ?: scheme.background,
            onBackground = onBackground ?: scheme.onBackground,
            surface = surface ?: scheme.surface,
            onSurface = onSurface ?: scheme.onSurface,
            surfaceVariant = surfaceVariant ?: scheme.surfaceVariant,
            onSurfaceVariant = onSurfaceVariant ?: scheme.onSurfaceVariant,
            surfaceTint = surfaceTint ?: scheme.surfaceTint,
            inverseSurface = inverseSurface ?: scheme.inverseSurface,
            inverseOnSurface = inverseOnSurface ?: scheme.inverseOnSurface,
            error = error ?: scheme.error,
            onError = onError ?: scheme.onError,
            errorContainer = errorContainer ?: scheme.errorContainer,
            onErrorContainer = onErrorContainer ?: scheme.onErrorContainer,
            outline = outline ?: scheme.outline,
            outlineVariant = outlineVariant ?: scheme.outlineVariant,
            scrim = scrim ?: scheme.scrim,
        )
    }
}

fun ColorScheme.toNullableColorScheme(): NullableColorScheme {
    return NullableColorScheme(
        primary, onPrimary, primaryContainer,
        onPrimaryContainer, inversePrimary, secondary,
        onSecondary, secondaryContainer
    )
}

@Composable
expect fun getThemeInfo(): ThemeInfo
