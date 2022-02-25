package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import tk.zwander.common.data.surface

@Composable
fun AlertDialogDef(
    onDismissRequest: () -> Unit,
    buttons: @Composable() () -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable() () -> Unit)?,
    text: (@Composable() () -> Unit)?,
    shape: Shape = RoundedCornerShape(8.dp),
    backgroundColor: Color = Color(surface.toLong(16)),
    contentColor: Color = contentColorFor(backgroundColor)
) {
    CAlertDialog(onDismissRequest, buttons, modifier, title, text, shape, backgroundColor, contentColor)
}

@Composable
fun AlertDialogContents(
    buttons: @Composable() () -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable() () -> Unit)?,
    text: (@Composable() () -> Unit)?,
    shape: Shape = RoundedCornerShape(8.dp),
    backgroundColor: Color,
    contentColor: Color,
) {
    Surface(
        shape = shape,
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp)
        ) {
            title?.let {
                it()

                Spacer(Modifier.size(8.dp))
            }

            text?.let {
                Column {
                    it()
                }

                Spacer(Modifier.size(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                buttons()
            }
        }
    }
}
