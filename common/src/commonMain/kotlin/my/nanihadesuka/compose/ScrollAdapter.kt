package my.nanihadesuka.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

abstract class LazyStateAdapter<T : ScrollableState>(
    thumbMinHeight: Float,
    alwaysShowScrollBar: Boolean,
    scrollableState: ScrollableState,
) : StateAdapter(thumbMinHeight, alwaysShowScrollBar, scrollableState) {
    abstract val totalItemsCount: Int
    abstract val firstVisibleItemIndex: Int
    abstract val realFirstVisibleItemIndex: Int?
    abstract val lastVisibleItemIndex: Int
    abstract val firstVisibleItemScrollOffset: Int
    abstract val viewportEndOffset: Int
    abstract val visibleItemsSize: Int
    abstract val isStickyHeaderInAction: Boolean
    abstract val reverseLayout: Boolean

    abstract fun fractionHiddenTop(index: Int, firstItemOffset: Int): Float
    abstract fun fractionVisibleBottom(index: Int, viewportEndOffset: Int): Float

    override val normalizedThumbSizeReal: Float
        get() {
            if (totalItemsCount == 0)
                return 0f

            val firstItem = realFirstVisibleItemIndex ?: return 0f
            val firstPartial =
                fractionHiddenTop(firstItem, firstVisibleItemScrollOffset)
            val lastPartial =
                1f - fractionVisibleBottom(lastVisibleItemIndex, viewportEndOffset)

            val realSize = visibleItemsSize - if (isStickyHeaderInAction) 1 else 0
            val realVisibleSize = realSize.toFloat() - firstPartial - lastPartial
            return realVisibleSize / totalItemsCount.toFloat()
        }

    override val normalizedOffsetPosition: Float
        get() {
            if (totalItemsCount == 0 || visibleItemsSize == 0)
                return 0f

            val firstItem = realFirstVisibleItemIndex ?: return 0f
            val top = (firstItem.toFloat() + fractionHiddenTop(firstItem, firstVisibleItemScrollOffset)) / totalItemsCount.toFloat()
            return offsetCorrection(top)
        }

    override val normalizedIndicatorContentOffset: Float
        get() = firstVisibleItemIndex.toFloat()


    override fun offsetCorrection(top: Float): Float {
        val topRealMax = (1f - normalizedThumbSizeReal).coerceIn(0f, 1f)
        if (normalizedThumbSizeReal >= thumbMinHeight) {
            return when {
                reverseLayout -> topRealMax - top
                else -> top
            }
        }

        val topMax = 1f - thumbMinHeight
        return when {
            reverseLayout -> (topRealMax - top) * topMax / topRealMax
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

abstract class StateAdapter(
    val thumbMinHeight: Float,
    val alwaysShowScrollBar: Boolean,
    val scrollableState: ScrollableState,
) {
    abstract val normalizedThumbSizeReal: Float
    abstract val normalizedOffsetPosition: Float
    abstract val normalizedIndicatorContentOffset: Float

    var isSelected by mutableStateOf(false)
    var isInActionSelectable by mutableStateOf(false)
    var dragOffsetState by mutableStateOf(0f)
        private set

    val normalizedThumbSize: Float get() = normalizedThumbSizeReal.coerceAtLeast(thumbMinHeight)
    val isInAction: Boolean get() = scrollableState.isScrollInProgress || isSelected || alwaysShowScrollBar
    val canScroll: Boolean get() = scrollableState.canScrollForward || scrollableState.canScrollBackward

    abstract fun setScrollOffset(newOffset: Float)
    abstract fun offsetCorrection(top: Float): Float
    abstract fun offsetCorrectionInverse(top: Float): Float

    fun setDragOffset(value: Float) {
        val maxValue = (1f - normalizedThumbSize).coerceAtLeast(0f)
        dragOffsetState = value.coerceIn(0f, maxValue)
    }
}

@Composable
fun BoxScope.ScrollAdapter(
    state: StateAdapter,
    modifier: Modifier = Modifier,
    rightSide: Boolean = true,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Thumb,
    selectionActionable: ScrollbarSelectionActionable = ScrollbarSelectionActionable.Always,
    hideDelayMillis: Int = 400,
    indicatorContent: (@Composable (normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)? = null,
) {
    val durationAnimationMillis = 500
    LaunchedEffect(state.isInAction) {
        if (state.isInAction) {
            state.isInActionSelectable = true
        } else {
            delay(timeMillis = durationAnimationMillis.toLong() + hideDelayMillis.toLong())
            state.isInActionSelectable = false
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (state.isInAction) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (state.isInAction) 75 else durationAnimationMillis,
            delayMillis = if (state.isInAction) 0 else hideDelayMillis
        ),
        label = "scrollbar alpha value"
    )

    val displacement by animateFloatAsState(
        targetValue = if (state.isInAction) 0f else 14f,
        animationSpec = tween(
            durationMillis = if (state.isInAction) 75 else durationAnimationMillis,
            delayMillis = if (state.isInAction) 0 else hideDelayMillis
        ),
        label = "scrollbar displacement value"
    )

    var currentHeight by remember {
        mutableStateOf(0)
    }

    Box(
        modifier = Modifier
            .matchParentSize()
            .onSizeChanged { currentHeight = it.height },
    ) {
        AnimatedVisibility(
            visible = currentHeight > 0 && state.normalizedThumbSize < 1.0f && state.canScroll,
            modifier = Modifier
                .matchParentSize(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            BoxWithConstraints(
                modifier = modifier
                    .matchParentSize(),
            ) {
                val thumbHeight by animateDpAsState(with (LocalDensity.current) { (state.normalizedThumbSize * currentHeight).toDp() })
                val animatedTranslation by animateFloatAsState(currentHeight * state.normalizedOffsetPosition)

                Box(
                    modifier = Modifier
                        .align(if (rightSide) Alignment.TopEnd else Alignment.TopStart)
                        .graphicsLayer {
                            translationX = (if (rightSide) displacement.dp else -displacement.dp).toPx()
                            translationY = animatedTranslation
                        },
                ) {
                    Box(
                        modifier = Modifier
                            .padding(
                                start = if (rightSide) 0.dp else padding,
                                end = if (!rightSide) 0.dp else padding,
                            )
                            .clip(thumbShape)
                            .width(thickness)
                            .height(thumbHeight)
                            .alpha(alpha)
                            .background(if (state.isSelected) thumbSelectedColor else thumbColor)
                            .align(if (rightSide) Alignment.TopEnd else Alignment.TopStart),
                    )

                    if (indicatorContent != null) {
                        Box(
                            modifier = Modifier
                                .alpha(alpha)
                                .height(with (LocalDensity.current) { currentHeight.toDp() })
                                .align(if (rightSide) Alignment.TopEnd else Alignment.TopStart),
                        ) {
                            indicatorContent(
                                state.normalizedIndicatorContentOffset,
                                state.isSelected,
                            )
                        }
                    }
                }

                if (
                    when (selectionActionable) {
                        ScrollbarSelectionActionable.Always -> true
                        ScrollbarSelectionActionable.WhenVisible -> state.isInActionSelectable
                    }
                ) {
                    DraggableBar(
                        padding = padding,
                        thickness = thickness,
                        height = currentHeight.toFloat(),
                        selectionMode = selectionMode,
                        state = state,
                        modifier = Modifier
                            .align(if (rightSide) Alignment.TopEnd else Alignment.TopStart)
                            .height(with (LocalDensity.current) { currentHeight.toDp() })
                            .wrapContentWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun DraggableBar(
    padding: Dp,
    thickness: Dp,
    height: Float,
    selectionMode: ScrollbarSelectionMode,
    state: StateAdapter,
    modifier: Modifier = Modifier,
) {
    val normalizedThumbSizeUpdated by rememberUpdatedState(state.normalizedThumbSize)

    Box(
        modifier = modifier
            .width(padding * 2 + thickness)
            .draggable(
                state = rememberDraggableState { delta ->
                    if (state.isSelected) {
                        state.setScrollOffset(state.dragOffsetState + delta / height)
                    }
                },
                orientation = Orientation.Vertical,
                enabled = selectionMode != ScrollbarSelectionMode.Disabled,
                startDragImmediately = true,
                onDragStarted = { offset ->
                    val newOffset = offset.y / height
                    val currentOffset = state.normalizedOffsetPosition

                    when (selectionMode) {
                        ScrollbarSelectionMode.Full -> {
                            if (newOffset in currentOffset..(currentOffset + normalizedThumbSizeUpdated))
                                state.setDragOffset(currentOffset)
                            else
                                state.setScrollOffset(newOffset)
                            state.isSelected = true
                        }

                        ScrollbarSelectionMode.Thumb -> {
                            if (newOffset in currentOffset..(currentOffset + normalizedThumbSizeUpdated)) {
                                state.setDragOffset(currentOffset)
                                state.isSelected = true
                            }
                        }

                        ScrollbarSelectionMode.Disabled -> Unit
                    }
                },
                onDragStopped = {
                    state.isSelected = false
                },
            ),
    )
}
