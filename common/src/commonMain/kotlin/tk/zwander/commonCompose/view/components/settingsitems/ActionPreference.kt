package tk.zwander.commonCompose.view.components.settingsitems

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tk.zwander.common.data.IOptionItem
import tk.zwander.commonCompose.view.components.LabelDesc
import tk.zwander.commonCompose.view.components.TransparencyCard

@Composable
fun ActionPreference(
    item: IOptionItem.ActionOptionItem,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    TransparencyCard(
        modifier = modifier,
        onClick = { scope.launch { item.action() } },
    ) {
        LabelDesc(
            item = item,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}