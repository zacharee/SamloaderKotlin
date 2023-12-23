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

                }
            }
        },
    )
}


