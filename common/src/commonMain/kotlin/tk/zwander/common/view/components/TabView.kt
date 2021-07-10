package tk.zwander.common.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Represents the available pages.
 */
enum class Page {
    DOWNLOADER,
    DECRYPTER,
    HISTORY
}

/**
 * Allows the user to switch among the different app pages.
 */
@Composable
fun TabView(page: MutableState<Page>) {
    TabRow(
        modifier = Modifier.fillMaxWidth()
            .height(48.dp),
        selectedTabIndex = page.value.ordinal
    ) {
        Tab(
            selected = page.value == Page.DOWNLOADER,
            text = { Text("Downloader") },
            onClick = {
                page.value = Page.DOWNLOADER
            }
        )
        Tab(
            selected = page.value == Page.DECRYPTER,
            text = { Text("Decrypter") },
            onClick = {
                page.value = Page.DECRYPTER
            }
        )
        Tab(
            selected = page.value == Page.HISTORY,
            text = { Text("History") },
            onClick = {
                page.value = Page.HISTORY
            }
        )
    }
}