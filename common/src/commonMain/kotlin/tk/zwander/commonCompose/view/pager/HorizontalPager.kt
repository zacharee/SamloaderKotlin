package tk.zwander.commonCompose.view.pager

import androidx.compose.material3.TabPosition
import androidx.compose.runtime.Composable
import tk.zwander.commonCompose.view.components.Page

@Composable
internal expect fun HorizontalPager(
    count: Int,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    eval: @Composable (Page) -> Unit,
): (@Composable (List<TabPosition>) -> Unit)?
