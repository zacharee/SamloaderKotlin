package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tk.zwander.samloaderkotlin.strings

/**
 * Represents the available pages.
 */
enum class Page(val index: Int) {
    DOWNLOADER(0),
    DECRYPTER(1),
    HISTORY(2)
}

/**
 * Allows the user to switch among the different app pages.
 */
@Composable
fun TabView(
    selectedPage: Page,
    onPageSelected: (Page) -> Unit,
    indicator: @Composable() ((List<TabPosition>) -> Unit)?
) {
    TabRow(
        modifier = Modifier.fillMaxWidth()
            .height(48.dp),
        selectedTabIndex = selectedPage.index,
        indicator = indicator ?: { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedPage.index])
            )
        }
    ) {
        Tab(
            selected = selectedPage == Page.DOWNLOADER,
            text = { Text(strings.downloader()) },
            onClick = {
                onPageSelected(Page.DOWNLOADER)
            }
        )
        Tab(
            selected = selectedPage == Page.DECRYPTER,
            text = { Text(strings.decrypter()) },
            onClick = {
                onPageSelected(Page.DECRYPTER)
            }
        )
        Tab(
            selected = selectedPage == Page.HISTORY,
            text = { Text(strings.history()) },
            onClick = {
                onPageSelected(Page.HISTORY)
            }
        )
    }
}
