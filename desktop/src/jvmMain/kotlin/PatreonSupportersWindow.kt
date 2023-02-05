import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import tk.zwander.common.util.PatreonSupportersParser
import tk.zwander.common.util.SupporterInfo
import tk.zwander.commonCompose.view.components.CustomMaterialTheme
import tk.zwander.commonCompose.view.components.PatreonSupportersList
import tk.zwander.samloaderkotlin.strings

@Composable
internal fun PatreonSupportersWindow(
    showing: Boolean,
    onDismiss: () -> Unit
) {
    if (showing) {
        val supporters = remember { mutableStateListOf<SupporterInfo>() }

        LaunchedEffect(key1 = null) {
            supporters.clear()
            supporters.addAll(PatreonSupportersParser.getInstance().parseSupporters())
        }

        Window(
            onCloseRequest = onDismiss,
            title = strings.patreonSupporters(),
            icon = getImage("icon.png").toPainter(),
            state = WindowState(
                placement = WindowPlacement.Floating,
                position = WindowPosition(Alignment.Center),
                size = DpSize(500.dp, 500.dp)
            ),
            resizable = false,
            alwaysOnTop = false
        ) {
            CustomMaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        PatreonSupportersList(
                            modifier = Modifier.align(Alignment.Center),
                            supporters = supporters
                        )
                    }
                }
            }
        }
    }
}
