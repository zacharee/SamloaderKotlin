import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import tk.zwander.common.data.appName
import tk.zwander.commonCompose.util.imageResource
import tk.zwander.commonCompose.view.components.CustomMaterialTheme
import tk.zwander.commonCompose.view.components.FooterView

@Composable
fun AboutDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (visible) {
        Window(
            onCloseRequest = onDismiss,
            title = appName,
            icon = getImage("icon.png").toPainter(),
            state = WindowState(
                placement = WindowPlacement.Floating,
                position = WindowPosition(Alignment.Center),
                size = DpSize(500.dp, Dp.Unspecified)
            ),
            resizable = false,
            alwaysOnTop = true
        ) {
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
                                text = appName,
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
