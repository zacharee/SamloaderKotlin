package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tk.zwander.common.data.PlatformFile

@Composable
expect fun Modifier.handleFileDrag(
    enabled: Boolean = true,
    onDragStart: (PlatformFile?) -> Unit = {},
    onDrag: (PlatformFile?) -> Unit = {},
    onDragExit: () -> Unit = {},
    onDrop: (PlatformFile?) -> Unit = {},
): Modifier