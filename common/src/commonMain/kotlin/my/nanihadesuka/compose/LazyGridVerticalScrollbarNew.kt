package my.nanihadesuka.compose

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.floor

/**
 * @param state LazyGridState
 * @param rightSide true -> right,  false -> left
 * @param thickness Thickness of the scrollbar thumb
 * @param padding Padding of the scrollbar
 * @param thumbMinHeight Thumb minimum height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 */
@Composable
fun LazyGridVerticalScrollbarNew(
    state: LazyGridState,
    rightSide: Boolean = true,
    alwaysShowScrollBar: Boolean = false,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbMinHeight: Float = 0.1f,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Thumb,
    selectionActionable: ScrollbarSelectionActionable = ScrollbarSelectionActionable.Always,
    hideDelayMillis: Int = 400,
    enabled: Boolean = true,
    indicatorContent: (@Composable (index: Float, isThumbSelected: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (!enabled) content()
    else BoxWithConstraints {
        val coroutineScope = rememberCoroutineScope()

        val realFirstVisibleItem by rememberUpdatedState(
            state.layoutInfo.visibleItemsInfo.firstOrNull {
                it.index == state.firstVisibleItemIndex
            }
        )

        val isStickyHeaderInAction by rememberUpdatedState(run {
            val realIndex = realFirstVisibleItem?.index ?: return@run false
            val firstVisibleIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                ?: return@run false
            realIndex != firstVisibleIndex
        })

        val stateAdapter = remember(thumbMinHeight, alwaysShowScrollBar, state) {
            object : StateAdapter(
                thumbMinHeight = thumbMinHeight,
                alwaysShowScrollBar = alwaysShowScrollBar,
                scrollableState = state,
            ) {
                fun LazyGridItemInfo.fractionHiddenTop(firstItemOffset: Int) =
                    if (size.height == 0) 0f else firstItemOffset / size.height.toFloat()

                fun LazyGridItemInfo.fractionVisibleBottom(viewportEndOffset: Int) =
                    if (size.height == 0) 0f else (viewportEndOffset - offset.y).toFloat() / size.height.toFloat()

                override val normalizedThumbSizeReal: Float
                    get() = state.layoutInfo.let {
                        if (it.totalItemsCount == 0)
                            return@let 0f

                        val firstItem = realFirstVisibleItem ?: return@let 0f
                        val firstPartial =
                            firstItem.fractionHiddenTop(state.firstVisibleItemScrollOffset)
                        val lastPartial =
                            1f - it.visibleItemsInfo.last().fractionVisibleBottom(it.viewportEndOffset)

                        val realSize = it.visibleItemsInfo.size - if (isStickyHeaderInAction) 1 else 0
                        val realVisibleSize = realSize.toFloat() - firstPartial - lastPartial
                        realVisibleSize / it.totalItemsCount.toFloat()
                    }

                override val normalizedOffsetPosition: Float
                    get() = state.layoutInfo.let {
                        if (it.totalItemsCount == 0 || it.visibleItemsInfo.isEmpty())
                            return@let 0f

                        val firstItem = realFirstVisibleItem ?: return@let 0f
                        val top = firstItem
                            .run { index.toFloat() + fractionHiddenTop(state.firstVisibleItemScrollOffset) } / it.totalItemsCount.toFloat()
                        offsetCorrection(top)
                    }

                override val normalizedIndicatorContentOffset: Float
                    get() = state.firstVisibleItemIndex.toFloat()

                override fun setScrollOffset(newOffset: Float) {
                    setDragOffset(newOffset)
                    val totalItemsCount = state.layoutInfo.totalItemsCount.toFloat()
                    val exactIndex = offsetCorrectionInverse(totalItemsCount * dragOffsetState)
                    val index: Int = floor(exactIndex).toInt()
                    val remainder: Float = exactIndex - floor(exactIndex)

                    coroutineScope.launch {
                        state.scrollToItem(index = index, scrollOffset = 0)
                        val offset = realFirstVisibleItem
                            ?.size
                            ?.let { it.height.toFloat() * remainder }
                            ?.toInt() ?: 0
                        state.scrollToItem(index = index, scrollOffset = offset)
                    }
                }

                override fun offsetCorrection(top: Float): Float {
                    val topRealMax = (1f - normalizedThumbSizeReal).coerceIn(0f, 1f)
                    if (normalizedThumbSizeReal >= thumbMinHeight) {
                        return when {
                            state.layoutInfo.reverseLayout -> topRealMax - top
                            else -> top
                        }
                    }

                    val topMax = 1f - thumbMinHeight
                    return when {
                        state.layoutInfo.reverseLayout -> (topRealMax - top) * topMax / topRealMax
                        else -> top * topMax / topRealMax
                    }
                }

                override fun offsetCorrectionInverse(top: Float): Float {
                    if (normalizedThumbSizeReal >= thumbMinHeight)
                        return top
                    val topRealMax = 1f - normalizedThumbSizeReal
                    val topMax = 1f - thumbMinHeight
                    return top * topRealMax / topMax
                }
            }
        }

        content()

        ScrollAdapter(
            state = stateAdapter,
            modifier = Modifier,
            rightSide = rightSide,
            thickness = thickness,
            padding = padding,
            thumbColor = thumbColor,
            thumbSelectedColor = thumbSelectedColor,
            thumbShape = thumbShape,
            selectionMode = selectionMode,
            selectionActionable = selectionActionable,
            hideDelayMillis = hideDelayMillis,
            indicatorContent = indicatorContent,
        )
    }
}
