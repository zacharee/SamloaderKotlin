package tk.zwander.commonCompose.view.pager

import androidx.compose.animation.Crossfade
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tk.zwander.commonCompose.view.components.Page

@Composable
internal actual fun HorizontalPager(
    count: Int,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    eval: @Composable (Page) -> Unit,
): (@Composable (List<TabPosition>) -> Unit)? {
    Crossfade(
        targetState = currentPage
    ) {
        eval(Page.values()[it])
    }

    return { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.tabIndicatorOffset(tabPositions[currentPage])
        )
    }
}
