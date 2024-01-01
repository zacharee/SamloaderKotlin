package tk.zwander.commonCompose.view.components

import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateIntOffset
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
internal fun SplitComponent(
    startComponent: @Composable () -> Unit,
    endComponent: @Composable () -> Unit,
    threshold: Dp = 600.dp,
    modifier: Modifier = Modifier,
    startRatio: Double = 0.5,
    endRatio: Double = 0.5,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val constraints = this.constraints
        val density = LocalDensity.current

        val (horizontalSpacingPx, verticalSpacingPx) = rememberSaveable(horizontalSpacing, verticalSpacing) {
            with(density) {
                horizontalSpacing.toPx() to verticalSpacing.toPx()
            }
        }

        val isOverThreshold = rememberSaveable(density, constraints.maxWidth) {
            (constraints.maxWidth / (density.density * density.fontScale)).dp > threshold
        }
        val transition = updateTransition(isOverThreshold)

        var initialLayoutDone by rememberSaveable(density) { mutableStateOf(false) }
        val fieldHeights = rememberSaveable(
            density,
            saver = listSaver(
                save = { it.toList() },
                restore = { it.toMutableStateList() },
            ),
        ) { mutableStateListOf(0, 0) }

        val layoutHeight by if (fieldHeights.isEmpty() || !initialLayoutDone) derivedStateOf {
            fieldHeights.sum().also {
                if (fieldHeights.isNotEmpty()) {
                    initialLayoutDone = true
                }
            }
        } else animateIntAsState(if (isOverThreshold) fieldHeights.max() else (fieldHeights.sum() + verticalSpacingPx).toInt())

        val startComponentWidth by derivedStateOf { constraints.maxWidth * startRatio - (horizontalSpacingPx / 2.0) }
        val endComponentWidth by derivedStateOf { constraints.maxWidth * endRatio - (horizontalSpacingPx / 2.0) }

        val endComponentOffset by if (!initialLayoutDone) derivedStateOf {
            IntOffset(
                x = if (isOverThreshold) (startComponentWidth + horizontalSpacingPx).toInt() else 0,
                y = if (isOverThreshold) 0 else (fieldHeights.getOrElse(0) { 0 } + verticalSpacingPx).toInt()
            )
        } else transition.animateIntOffset {
            IntOffset(
                x = if (it) (startComponentWidth + horizontalSpacingPx).toInt() else 0,
                y = if (it) 0 else (fieldHeights.getOrElse(0) { 0 } + verticalSpacingPx).toInt()
            )
        }

        val startComponentAnimWidth by if (!initialLayoutDone) derivedStateOf {
            if (isOverThreshold) startComponentWidth.toInt() else constraints.maxWidth
        } else transition.animateInt {
            if (it) startComponentWidth.toInt() else constraints.maxWidth
        }
        val endComponentAnimWidth by if (!initialLayoutDone) derivedStateOf {
            if (isOverThreshold) endComponentWidth.toInt() else constraints.maxWidth
        } else transition.animateInt {
            if (it) endComponentWidth.toInt() else constraints.maxWidth
        }

        val lHeight by rememberUpdatedState(layoutHeight)
        val eOffset by rememberUpdatedState(endComponentOffset)
        val sWidth by rememberUpdatedState(startComponentAnimWidth)
        val eWidth by rememberUpdatedState(endComponentAnimWidth)

        Layout(
            content = {
                startComponent()
                endComponent()
            }
        ) { measurables, c ->
            layout(c.maxWidth, lHeight) {
                measurables.forEachIndexed { index, measurable ->
                    val placeable = measurable.measure(
                        Constraints(
                            maxWidth = if (index == 0) sWidth else eWidth,
                            minWidth = if (index == 0) sWidth else eWidth
                        )
                    )

                    fieldHeights[index] = placeable.height

                    placeable.place(if (index == 0) IntOffset.Zero else eOffset)
                }
            }
        }
    }
}
