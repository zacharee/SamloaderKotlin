package tk.zwander.commonCompose.util

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import dev.zwander.kotlin.file.PlatformFile
import java.io.IOException
import java.net.URI
import kotlin.io.path.toPath

@Composable
actual fun Modifier.handleFileDrag(
    enabled: Boolean,
    onDrop: (PlatformFile?) -> Boolean,
): Modifier = composed {
    val target = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                return onDrop(event.extractFile())
            }
        }
    }

    dragAndDropTarget(
        shouldStartDragAndDrop = { enabled },
        target = target,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
private fun DragAndDropEvent.extractFile(): PlatformFile? {
    return try {
        val filesList = dragData() as? DragData.FilesList?
        val files = filesList?.readFiles()
        val firstFile = files?.firstOrNull()

        firstFile?.let { PlatformFile(URI.create(it).toPath().toFile()) }
    } catch (e: NullPointerException) {
        null
    } catch (e: IOException) {
        null
    }
}