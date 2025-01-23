package my.nanihadesuka.compose

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @param thumbMinLength Thumb minimum length proportional to total scrollbar's length (eg: 0.1 -> 10% of total)
 */
@Stable
data class ScrollbarSettings(
    val enabled: Boolean = Default.enabled,
    val side: ScrollbarLayoutSide = Default.side,
    val alwaysShowScrollbar: Boolean = Default.alwaysShowScrollbar,
    val scrollbarPadding: Dp = Default.scrollbarPadding,
    val thumbThickness: Dp = Default.thumbThickness,
    val thumbShape: Shape = Default.thumbShape,
    val thumbMinLength: Float = Default.thumbMinLength,
    val thumbMaxLength: Float = Default.thumbMaxLength,
    val thumbUnselectedColor: Color = Default.thumbUnselectedColor,
    val thumbSelectedColor: Color = Default.thumbSelectedColor,
    val selectionMode: ScrollbarSelectionMode = Default.selectionMode,
    val selectionActionable: ScrollbarSelectionActionable = Default.selectionActionable,
    val hideDelayMillis: Int = Default.hideDelayMillis,
    val hideDisplacement: Dp = Default.hideDisplacement,
    val hideEasingAnimation: Easing = Default.hideEasingAnimation,
    val durationAnimationMillis: Int = Default.durationAnimationMillis,
) {
    init {
        require(thumbMinLength <= thumbMaxLength) {
            "thumbMinLength ($thumbMinLength) must be less or equal to thumbMaxLength ($thumbMaxLength)"
        }
    }

    companion object {
        val Default = ScrollbarSettings(
            enabled = true,
            side = ScrollbarLayoutSide.End,
            alwaysShowScrollbar = false,
            thumbThickness = 6.dp,
            scrollbarPadding = 8.dp,
            thumbMinLength = 0.1f,
            thumbMaxLength = 1.0f,
            thumbUnselectedColor = Color(0xFF2A59B6),
            thumbSelectedColor = Color(0xFF5281CA),
            thumbShape = CircleShape,
            selectionMode = ScrollbarSelectionMode.Thumb,
            selectionActionable = ScrollbarSelectionActionable.Always,
            hideDelayMillis = 400,
            hideDisplacement = 14.dp,
            hideEasingAnimation = FastOutSlowInEasing,
            durationAnimationMillis = 500,
        )
    }
}
