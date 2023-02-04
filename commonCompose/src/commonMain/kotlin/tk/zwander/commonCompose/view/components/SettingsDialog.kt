package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
import com.soywiz.korio.util.OS
import tk.zwander.samloaderkotlin.strings

@Composable
fun SettingsDialog(
    showing: Boolean,
    options: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    val settings = remember { Settings() }

    AlertDialogDef(
        showing = showing,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        buttons = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = strings.ok())
            }
        },
        title = {
            Text(strings.settings())
        },
        text = {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                options.forEach { opt ->
                    var checked by remember { mutableStateOf(settings.getBoolean(opt.second, false)) }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(opt.first)
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = checked,
                            onCheckedChange = {
                                checked = it
                                settings.putBoolean(opt.second, it)
                            }
                        )
                    }
                }
            }
        }
    )
}
