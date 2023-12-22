package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tk.zwander.common.data.changelog.Changelog
import tk.zwander.common.util.invoke
import tk.zwander.commonCompose.util.parseHtml
import tk.zwander.samloaderkotlin.resources.MR

@Composable
internal fun ChangelogDisplay(
    changelog: Changelog?,
) {
    Card(
        elevation = CardDefaults.cardElevation(),
        border = BorderStroke(1.dp, Color(255, 255, 255, 100)),
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp)
        ) {
            val infoItems by derivedStateOf {
                val list = mutableListOf<String>()

                changelog?.relDate?.let { relDate ->
                    list.add(MR.strings.release(relDate))
                }

                changelog?.secPatch?.let { secPatch ->
                    list.add(MR.strings.security(secPatch))
                }

                list
            }

            if (infoItems.isNotEmpty()) {
                Text(
                    text = infoItems.joinToString("  •  "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )

            }

            if (changelog?.notes != null) {
                SelectionContainer {
                    Text(changelog.notes.parseHtml())
                }
            }
        }
    }
}
