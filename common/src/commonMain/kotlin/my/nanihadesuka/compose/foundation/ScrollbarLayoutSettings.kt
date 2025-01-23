package my.nanihadesuka.compose.foundation

import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import my.nanihadesuka.compose.ScrollbarLayoutSide
import my.nanihadesuka.compose.ScrollbarSelectionActionable

@Stable
internal data class ScrollbarLayoutSettings(
    val scrollbarPadding: Dp,
    val thumbShape: Shape,
    val thumbThickness: Dp,
    val thumbUnselectedColor: Color,
    val thumbSelectedColor: Color,
    val side: ScrollbarLayoutSide,
    val selectionActionable: ScrollbarSelectionActionable,
    val hideDisplacement: Dp,
    val hideDelayMillis: Int,
    val hideEasingAnimation: Easing,
    val durationAnimationMillis: Int
)