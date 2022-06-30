package tk.zwander.commonCompose.util.pager

import androidx.compose.runtime.Composable
import tk.zwander.commonCompose.view.components.Page

@Composable
actual fun HorizontalPager(
    state: PagerState,
    count: Int,
    eval: @Composable() (Page) -> Unit,
) {

}
