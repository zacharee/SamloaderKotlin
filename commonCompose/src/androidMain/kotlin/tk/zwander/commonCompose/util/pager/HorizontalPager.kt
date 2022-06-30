package tk.zwander.commonCompose.util.pager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import tk.zwander.commonCompose.view.components.Page

@OptIn(ExperimentalPagerApi::class)
@Composable
actual fun HorizontalPager(
    state: PagerState,
    count: Int,
    eval: @Composable() (Page) -> Unit,
) {
    val pState = com.google.accompanist.pager.rememberPagerState()

    LaunchedEffect(key1 = pState.currentPage) {
        state.currentPage = pState.currentPage
    }

    com.google.accompanist.pager.HorizontalPager(
        count = count,
        state = pState,
        itemSpacing = 8.dp
    ) {
        eval(Page.values()[it])
    }
}
