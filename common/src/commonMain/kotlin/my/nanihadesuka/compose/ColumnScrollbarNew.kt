package my.nanihadesuka.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Scrollbar for Column
 *
 * @param rightSide true -> right,  false -> left
 * @param thickness Thickness of the scrollbar thumb
 * @param padding   Padding of the scrollbar
 * @param thumbMinHeight Thumb minimum height proportional to total scrollbar's height (eg: 0.1 -> 10% of total)
 */
@Composable
fun ColumnScrollbarNew(
    state: ScrollState,
    rightSide: Boolean = true,
    alwaysShowScrollBar: Boolean = false,
    thickness: Dp = 6.dp,
    padding: Dp = 8.dp,
    thumbMinHeight: Float = 0.1f,
    thumbColor: Color = Color(0xFF2A59B6),
    thumbSelectedColor: Color = Color(0xFF5281CA),
    thumbShape: Shape = CircleShape,
    enabled: Boolean = true,
    selectionMode: ScrollbarSelectionMode = ScrollbarSelectionMode.Thumb,
    selectionActionable: ScrollbarSelectionActionable = ScrollbarSelectionActionable.Always,
    hideDelayMillis: Int = 400,
    indicatorContent: (@Composable (normalizedOffset: Float, isThumbSelected: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var currentHeight by remember {
        mutableStateOf(0)
    }

    if (!enabled) content()
    else BoxWithConstraints(
        modifier = Modifier.onSizeChanged { currentHeight = it.height },
    ) {
        val coroutineScope = rememberCoroutineScope()
        val visibleHeightDp by rememberUpdatedState(with(LocalDensity.current) { currentHeight.toDp() })
        val fullHeightDp by rememberUpdatedState(with(LocalDensity.current) { state.maxValue.toDp() + visibleHeightDp })

        val stateAdapter = remember(thumbMinHeight, alwaysShowScrollBar, state) {
            object : StateAdapter(
                thumbMinHeight = thumbMinHeight,
                alwaysShowScrollBar = alwaysShowScrollBar,
                scrollableState = state,
            ) {
                override val normalizedThumbSizeReal: Float
                    get() = if (fullHeightDp == 0.dp) 1f else {
                        val normalizedDp = visibleHeightDp / fullHeightDp
                        normalizedDp.coerceIn(0f, 1f)
                    }

                override val normalizedIndicatorContentOffset: Float
                    get() = offsetCorrectionInverse(normalizedOffsetPosition)

                override val normalizedOffsetPosition: Float
                    get() {
                        if (state.maxValue == 0) return 0f
                        val normalized = state.value.toFloat() / state.maxValue.toFloat()
                        return offsetCorrection(normalized)
                    }

                override fun offsetCorrectionInverse(top: Float): Float {
                    val topRealMax = 1f
                    val topMax = 1f - normalizedThumbSize
                    if (topMax == 0f) return top
                    return (top * topRealMax / topMax).coerceAtLeast(0f)
                }

                override fun offsetCorrection(top: Float): Float {
                    val topRealMax = 1f
                    val topMax = (1f - normalizedThumbSize).coerceIn(0f, 1f)
                    return top * topMax / topRealMax
                }

                override fun setScrollOffset(newOffset: Float) {
                    setDragOffset(newOffset)
                    val exactIndex = offsetCorrectionInverse(state.maxValue * dragOffsetState).toInt()
                    coroutineScope.launch {
                        state.scrollTo(exactIndex)
                    }
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
