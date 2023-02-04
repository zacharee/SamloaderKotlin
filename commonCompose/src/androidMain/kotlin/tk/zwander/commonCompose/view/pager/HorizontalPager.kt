package tk.zwander.commonCompose.view.pager

import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.pagerTabIndicatorOffset
import tk.zwander.commonCompose.view.components.Page
import kotlin.jvm.internal.DefaultConstructorMarker

private val constructor = Class.forName("androidx.compose.material.TabPosition")
    .getDeclaredConstructor(Float::class.java, Float::class.java)
    .apply { isAccessible = true }

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
                constructor.newInstance(it.left.value, it.width.value) as androidx.compose.material.TabPosition
            })
        )
    }
}
