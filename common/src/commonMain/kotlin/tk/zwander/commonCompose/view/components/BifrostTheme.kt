package tk.zwander.commonCompose.view.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.zwander.compose.DynamicMaterialTheme
import tk.zwander.common.util.BifrostSettings
import tk.zwander.commonCompose.view.LocalUseTransparencyEffects

@Composable
fun BifrostTheme(block: @Composable () -> Unit) {
    val useTransparency by BifrostSettings.useTransparencyEffects.collectAsState(false)

    DynamicMaterialTheme {
        CompositionLocalProvider(
            LocalUseTransparencyEffects provides useTransparency,
        ) {
            block()
        }
    }
}
