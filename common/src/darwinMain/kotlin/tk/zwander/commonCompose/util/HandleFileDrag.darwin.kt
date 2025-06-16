package tk.zwander.commonCompose.util

import androidx.compose.ui.Modifier
import dev.zwander.kotlin.file.PlatformFile

@androidx.compose.runtime.Composable
actual fun Modifier.handleFileDrag(
    enabled: Boolean,
    onDrop: (PlatformFile?) -> Boolean
): Modifier {
    return this
}