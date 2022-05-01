package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soywiz.korio.util.OS
import tk.zwander.common.res.Strings
import tk.zwander.commonCompose.util.pager.PagerState
import tk.zwander.commonCompose.util.pager.pagerTabIndicatorOffset

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
    pagerState: PagerState,
    selectedPage: Page,
    onPageSelected: (Page) -> Unit,
) {
    TabRow(
        modifier = Modifier.fillMaxWidth()
            .height(48.dp),
        selectedTabIndex = selectedPage.index,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }
    ) {
        Tab(
            selected = selectedPage == Page.DOWNLOADER,
            text = { Text(Strings.downloader) },
            onClick = {
                onPageSelected(Page.DOWNLOADER)
            }
        )
        Tab(
            selected = selectedPage == Page.DECRYPTER,
            text = { Text(Strings.decrypter) },
            onClick = {
                onPageSelected(Page.DECRYPTER)
            }
        )
        Tab(
            selected = selectedPage == Page.HISTORY,
            text = { Text(Strings.history) },
            onClick = {
                onPageSelected(Page.HISTORY)
            }
        )
    }
}