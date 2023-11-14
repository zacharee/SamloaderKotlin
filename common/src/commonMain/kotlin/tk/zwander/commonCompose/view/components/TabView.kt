package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tk.zwander.common.util.invoke
import tk.zwander.samloaderkotlin.resources.MR

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
internal fun TabView(
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
            text = { Text(MR.strings.downloader()) },
            onClick = {
                onPageSelected(Page.DOWNLOADER)
            }
        )
        Tab(
            selected = selectedPage == Page.DECRYPTER,
            text = { Text(MR.strings.decrypter()) },
            onClick = {
                onPageSelected(Page.DECRYPTER)
            }
        )
        Tab(
            selected = selectedPage == Page.HISTORY,
            text = { Text(MR.strings.history()) },
            onClick = {
                onPageSelected(Page.HISTORY)
            }
        )
    }
}
