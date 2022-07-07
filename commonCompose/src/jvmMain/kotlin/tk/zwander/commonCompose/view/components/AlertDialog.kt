package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun CAlertDialog(
    onDismissRequest: () -> Unit,
    buttons: @Composable() () -> Unit,
    modifier: Modifier,
    title: (@Composable() () -> Unit)?,
    text: (@Composable() () -> Unit)?,
    shape: Shape,
    backgroundColor: Color,
    contentColor: Color
) {
    Dialog(
        onCloseRequest = onDismissRequest,
        undecorated = true,
        transparent = true,
        onKeyEvent = {
            if (it.key == Key.Escape) {
                onDismissRequest()
                true
            } else {
                false
            }
        }
    ) {
        WindowDraggableArea {
            AlertDialogContents(
                buttons, modifier, title, text, shape, backgroundColor, contentColor
            )
        }
    }
}
