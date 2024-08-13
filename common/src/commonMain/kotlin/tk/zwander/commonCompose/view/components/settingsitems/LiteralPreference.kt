package tk.zwander.commonCompose.view.components.settingsitems

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tk.zwander.common.data.IOptionItem
import tk.zwander.commonCompose.view.components.LabelDesc
import tk.zwander.commonCompose.view.components.TransparencyCard

@Composable
fun LiteralPreference(
    item: IOptionItem.LiteralOptionItem,
    modifier: Modifier = Modifier,
) {
    TransparencyCard(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            LabelDesc(
                item = item,
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
            )

            item.render(Modifier.fillMaxWidth())
        }
    }
}
