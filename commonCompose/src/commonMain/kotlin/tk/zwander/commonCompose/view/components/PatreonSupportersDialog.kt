package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soywiz.korio.async.launch
import tk.zwander.common.util.PatreonSupportersParser
import tk.zwander.common.util.SupporterInfo
import tk.zwander.samloaderkotlin.strings

@Composable
fun PatreonSupportersDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    val supporters = remember { mutableStateListOf<SupporterInfo>() }

    LaunchedEffect(key1 = null) {
        supporters.clear()
        supporters.addAll(PatreonSupportersParser.getInstance().parseSupporters())
    }

    AlertDialogDef(
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
            Text(text = strings.patreonSupporters())
        },
        text = {
            PatreonSupportersList(
                supporters = supporters,
                modifier = Modifier.heightIn(min = 200.dp)
            )
        }
    )
}
