package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
expect fun CAlertDialog(
    onDismissRequest: () -> Unit,
    buttons: @Composable() () -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable() () -> Unit)?,
    text: (@Composable() () -> Unit)?,
    shape: Shape = RoundedCornerShape(8.dp),
    backgroundColor: Color,
    contentColor: Color,
)
