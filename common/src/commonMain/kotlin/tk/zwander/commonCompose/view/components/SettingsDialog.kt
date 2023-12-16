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
import tk.zwander.common.util.SettingsKey
import tk.zwander.common.util.invoke
import tk.zwander.samloaderkotlin.resources.MR

@Composable
internal fun SettingsDialog(
    showing: Boolean,
    options: List<Pair<String, SettingsKey<*>>>,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    AlertDialogDef(
        showing = showing,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        buttons = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = MR.strings.ok())
            }
        },
        title = {
            Text(MR.strings.settings())
        },
        text = {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                options.forEach { (label, key) ->
                    when (key) {
                        is SettingsKey.Boolean -> {
                            BooleanPreference(
                                label = label,
                                key = key,
                            )
                        }
                        // TODO: Layouts for other settings types.
                        else -> {}
                    }
                }
            }
        },
    )
}

@Composable
private fun BooleanPreference(
    label: String,
    key: SettingsKey.Boolean,
    modifier: Modifier = Modifier,
) {
    var state by key.collectAsMutableState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(text = label)

        Spacer(modifier = Modifier.weight(1f))

        Switch(
            checked = state ?: false,
            onCheckedChange = {
                state = it
            },
        )
    }
}
