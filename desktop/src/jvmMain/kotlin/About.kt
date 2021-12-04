import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPainter
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
                size = DpSize(500.dp, 300.dp)
            ),
            resizable = false,
            alwaysOnTop = true
        ) {
            CustomMaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            bitmap = imageResource("icon.png"),
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Text(
                            text = appName,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        FooterView()
                    }
                }
            }
        }
    }
}
