import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import tk.zwander.common.GradleConfig
import tk.zwander.commonCompose.util.imageResource
import tk.zwander.commonCompose.view.components.CustomMaterialTheme
import tk.zwander.commonCompose.view.components.FooterView

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AboutDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (visible) {
        Window(
            onCloseRequest = onDismiss,
            title = GradleConfig.appName,
            icon = getImage("icon.png").toPainter(),
            state = WindowState(
                placement = WindowPlacement.Floating,
                position = WindowPosition(Alignment.Center),
                size = DpSize(500.dp, Dp.Unspecified)
            ),
            resizable = false,
            onKeyEvent = {
                if (it.key == Key.Escape) {
                    onDismiss()
                    true
                } else {
                    false
                }
            },
        ) {
            WindowDraggableArea {
                CustomMaterialTheme {
                    Surface {
                        Box(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column {
                                Image(
                                    bitmap = imageResource("icon.png"),
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )

                                Text(
                                    text = GradleConfig.appName,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )

                                Spacer(modifier = Modifier.size(16.dp))

                                FooterView()
                            }
                        }
                    }
                }
            }
        }
    }
}
