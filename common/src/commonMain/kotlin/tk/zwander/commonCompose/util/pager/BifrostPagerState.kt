package tk.zwander.commonCompose.util.pager

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.pager.PagerState
import tk.zwander.commonCompose.view.components.Page
import tk.zwander.commonCompose.view.components.pages

class BifrostPagerState : PagerState() {
    override val pageCount: Int
        get() = pages.size

    @Suppress("unused")
    val currentBifrostPage: Page
        get() = pages[currentPage]

    suspend fun animateScrollToPage(
        page: Page,
        pageOffsetFraction: Float = 0f,
        animationSpec: AnimationSpec<Float> = spring()
    ) {
        animateScrollToPage(page.index, pageOffsetFraction, animationSpec)
    }
}