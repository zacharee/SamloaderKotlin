package tk.zwander.commonCompose.view.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import dev.zwander.compose.DynamicMaterialTheme
import tk.zwander.common.util.BifrostSettings
import tk.zwander.commonCompose.view.LocalUseMicaEffect

@Composable
fun BifrostTheme(block: @Composable () -> Unit) {
    val useMicaEffect by BifrostSettings.Keys.useMicaEffect.collectAsMutableState()

    DynamicMaterialTheme {
        CompositionLocalProvider(
            LocalUseMicaEffect provides useMicaEffect,
        ) {
            block()
        }
    }
}
