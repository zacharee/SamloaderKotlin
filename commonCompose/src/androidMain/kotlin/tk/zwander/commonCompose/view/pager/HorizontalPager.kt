package tk.zwander.commonCompose.view.pager

import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.pagerTabIndicatorOffset
import tk.zwander.commonCompose.view.components.Page

@OptIn(ExperimentalPagerApi::class)
@Composable
actual fun HorizontalPager(
    count: Int,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    eval: @Composable() (Page) -> Unit,
): (@Composable() (List<TabPosition>) -> Unit)? {
    val pState = com.google.accompanist.pager.rememberPagerState()

    LaunchedEffect(key1 = currentPage) {
        pState.animateScrollToPage(currentPage)
    }

    LaunchedEffect(key1 = pState.currentPage) {
        onPageChanged(pState.currentPage)
    }

    com.google.accompanist.pager.HorizontalPager(
        count = count,
        state = pState,
        itemSpacing = 8.dp
    ) {
        eval(Page.values()[it])
    }

    return { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.pagerTabIndicatorOffset(pState, tabPositions.map {
                Class.forName("androidx.compose.material.TabPosition")
                    .getConstructor(Dp::class.java, Dp::class.java)
                    .apply { isAccessible = true }
                    .newInstance(it.left, it.right) as androidx.compose.material.TabPosition
            })
        )
    }
}
