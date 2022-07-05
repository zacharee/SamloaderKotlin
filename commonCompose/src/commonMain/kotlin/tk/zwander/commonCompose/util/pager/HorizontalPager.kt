package tk.zwander.commonCompose.util.pager

import androidx.compose.material.TabPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tk.zwander.commonCompose.view.components.Page

@Composable
expect fun HorizontalPager(
    count: Int,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    eval: @Composable() (Page) -> Unit,
): (@Composable() (List<TabPosition>) -> Unit)?
