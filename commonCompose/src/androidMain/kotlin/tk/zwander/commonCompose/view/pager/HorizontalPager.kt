package tk.zwander.commonCompose.view.pager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tk.zwander.commonCompose.util.pager.pagerTabIndicatorOffset
import tk.zwander.commonCompose.view.components.Page

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal actual fun HorizontalPager(
    count: Int,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    eval: @Composable() (Page) -> Unit,
): (@Composable() (List<TabPosition>) -> Unit)? {
    val pState = androidx.compose.foundation.pager.rememberPagerState()

    LaunchedEffect(key1 = currentPage) {
        pState.animateScrollToPage(currentPage)
    }

    LaunchedEffect(key1 = pState.currentPage) {
        if (!pState.isScrollInProgress) {
            onPageChanged(pState.currentPage)
        }
    }

    androidx.compose.foundation.pager.HorizontalPager(
        pageCount = count,
        state = pState,
        pageSpacing = 8.dp,
    ) {
        eval(Page.values()[it])
    }

    return { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.pagerTabIndicatorOffset(pState, tabPositions)
        )
    }
}
