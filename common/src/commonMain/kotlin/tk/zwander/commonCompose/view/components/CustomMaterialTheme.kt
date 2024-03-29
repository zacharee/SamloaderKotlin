package tk.zwander.commonCompose.view.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import tk.zwander.common.util.BifrostSettings
import tk.zwander.commonCompose.util.FontMapper
import tk.zwander.commonCompose.util.rememberThemeInfo
import tk.zwander.commonCompose.view.LocalUseMicaEffect

/**
 * A Material theme with custom colors and such.
 */
@Composable
fun CustomMaterialTheme(block: @Composable () -> Unit) {
    val themeInfo = rememberThemeInfo()
    val useMicaEffect by BifrostSettings.Keys.useMicaEffect.collectAsMutableState()

    MaterialTheme(
        colorScheme = themeInfo.colors,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(
                fontFamily = LocalTextStyle.current.fontFamily?.let {
                    FontMapper.mapGenericFontFamilyToSpecificFontFamily(it)
                },
            ),
            LocalUseMicaEffect provides (useMicaEffect == true),
        ) {
            block()
        }
    }
}
