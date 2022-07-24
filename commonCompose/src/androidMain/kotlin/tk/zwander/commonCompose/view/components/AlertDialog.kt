package tk.zwander.commonCompose.view.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.window.Dialog

@Composable
actual fun CAlertDialog(
    showing: Boolean,
    onDismissRequest: () -> Unit,
    buttons: @Composable() () -> Unit,
    modifier: Modifier,
    title: (@Composable() () -> Unit)?,
    text: (@Composable() () -> Unit)?,
    shape: Shape,
    backgroundColor: Color,
    contentColor: Color
) {
    if (showing) {
        Dialog(
            onDismissRequest = onDismissRequest,
        ) {
            AlertDialogContents(
                buttons, modifier, title, text, shape, backgroundColor, contentColor
            )
        }
    }
}
