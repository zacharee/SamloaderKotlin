package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zwander.kotlin.file.PlatformFile

@Composable
expect fun Modifier.handleFileDrag(
    enabled: Boolean = true,
    onDrop: (PlatformFile?) -> Boolean = { false },
): Modifier