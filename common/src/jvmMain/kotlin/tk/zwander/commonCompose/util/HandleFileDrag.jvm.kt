package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ExternalDragValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.onExternalDrag
import tk.zwander.common.data.PlatformFile
import java.net.URI
import kotlin.io.path.toPath

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.handleFileDrag(
    enabled: Boolean,
    onDragStart: (PlatformFile?) -> Unit,
    onDrag: (PlatformFile?) -> Unit,
    onDragExit: () -> Unit,
    onDrop: (PlatformFile?) -> Unit
): Modifier {
    return onExternalDrag(
        enabled = enabled,
        onDragStart = { onDragStart(it.extractFile()) },
        onDrag = { onDrag(it.extractFile()) },
        onDragExit = onDragExit,
        onDrop = { onDrop(it.extractFile()) },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
private fun ExternalDragValue.extractFile(): PlatformFile? {
    return try {
        val filesList = dragData as? DragData.FilesList?
        val files = filesList?.readFiles()
        val firstFile = files?.firstOrNull()

        firstFile?.let { PlatformFile(URI.create(it).toPath().toFile()) }
    } catch (e: NullPointerException) {
        null
    }
}