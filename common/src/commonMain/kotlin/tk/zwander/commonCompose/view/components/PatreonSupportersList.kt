package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tk.zwander.common.util.SupporterInfo
import tk.zwander.common.util.UrlHandler

@Composable
fun PatreonSupportersList(
    modifier: Modifier = Modifier,
    supporters: List<SupporterInfo>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(supporters.size, { it }) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clickable {
                            UrlHandler.launchUrl(supporters[it].link)
                        }
                        .heightIn(min = 64.dp)
                        .padding(8.dp)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = supporters[it].name
                    )
                }
            }
        }
    }
}
