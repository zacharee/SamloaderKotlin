package tk.zwander.commonCompose.view.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tk.zwander.common.data.PlatformFile

@Composable
actual fun Modifier.handleFileDrag(
    enabled: Boolean,
    onDragStart: (PlatformFile?) -> Unit,
    onDrag: (PlatformFile?) -> Unit,
    onDragExit: () -> Unit,
    onDrop: (PlatformFile?) -> Unit
): Modifier {
    // No-op
    return this
}