package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
internal fun AlertDialogDef(
    showing: Boolean,
    onDismissRequest: () -> Unit,
    buttons: @Composable() RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable() () -> Unit)?,
    text: (@Composable() ColumnScope.() -> Unit)?,
    shape: Shape = RoundedCornerShape(8.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor)
) {
    CAlertDialog(showing, onDismissRequest, buttons, modifier, title, text, shape, backgroundColor, contentColor)
}

@Composable
internal fun AlertDialogContents(
    buttons: @Composable() RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable() () -> Unit)?,
    text: (@Composable() ColumnScope.() -> Unit)?,
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
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                    val textStyle = MaterialTheme.typography.headlineSmall
                    ProvideTextStyle(textStyle) {
                        it()
                    }
                }

                Spacer(Modifier.size(8.dp))
            }

            text?.let {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    it()
                }

                Spacer(Modifier.size(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))

                buttons()
            }
        }
    }
}
