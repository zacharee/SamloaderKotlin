package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.soywiz.korio.async.launch
import tk.zwander.common.res.Strings
import tk.zwander.common.util.PatreonSupportersParser
import tk.zwander.common.util.SupporterInfo

@Composable
fun PatreonSupportersDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val supporters = remember { mutableStateListOf<SupporterInfo>() }

    scope.launch {
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
                Text(text = Strings.ok)
            }
        },
        title = {
            Text(text = Strings.patreonSupporters)
        },
        text = {
            PatreonSupportersList(
                supporters = supporters,
                modifier = Modifier.fillMaxHeight()
            )
        }
    )
}
