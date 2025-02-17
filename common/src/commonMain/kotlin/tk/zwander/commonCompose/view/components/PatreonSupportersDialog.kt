package tk.zwander.commonCompose.view.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.compose.alertdialog.InWindowAlertDialog
import tk.zwander.common.util.PatreonSupportersParser
import tk.zwander.common.util.SupporterInfo
import tk.zwander.samloaderkotlin.resources.MR

@Composable
internal fun PatreonSupportersDialog(
    showing: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    val supporters = remember { mutableStateListOf<SupporterInfo>() }

    LaunchedEffect(key1 = null) {
        supporters.clear()

        try {
            supporters.addAll(PatreonSupportersParser.getInstance().parseSupporters())
        } catch (_: Exception) {}
    }

    InWindowAlertDialog(
        showing = showing,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        buttons = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(MR.strings.ok))
            }
        },
        title = {
            Text(text = stringResource(MR.strings.patreonSupporters))
        },
        text = {
            PatreonSupportersList(
                supporters = supporters,
            )
        },
        contentsScrollable = false,
    )
}
