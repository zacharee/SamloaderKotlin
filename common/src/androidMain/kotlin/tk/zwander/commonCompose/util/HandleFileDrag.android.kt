package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zwander.kotlin.file.PlatformFile

@Composable
actual fun Modifier.handleFileDrag(
    enabled: Boolean,
    onDragStart: (PlatformFile?) -> Unit,
    onDrag: (PlatformFile?) -> Unit,
    onDragExit: () -> Unit,
    onDrop: (PlatformFile?) -> Unit,
): Modifier {
    // No-op
    return this
}