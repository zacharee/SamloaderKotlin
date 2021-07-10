package tk.zwander.common.view.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tk.zwander.common.data.changelog.Changelog
import tk.zwander.common.util.parseHtml

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChangelogDisplay(
    changelog: Changelog
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (changelog.secPatch != null) {
            Text(
                fontWeight = FontWeight.Bold,
                text = "Security: ${changelog.secPatch}",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
        }

        if (changelog.relDate != null) {
            Text(
                fontWeight = FontWeight.Bold,
                text = "Release: ${changelog.relDate}",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
        }

        if (changelog.notes != null) {
            Text(changelog.notes.parseHtml())
        }
    }
}