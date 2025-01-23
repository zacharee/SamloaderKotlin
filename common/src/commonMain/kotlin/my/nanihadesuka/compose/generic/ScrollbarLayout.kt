package my.nanihadesuka.compose.generic

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import my.nanihadesuka.compose.foundation.HorizontalScrollbarLayout
import my.nanihadesuka.compose.foundation.ScrollbarLayoutSettings
import my.nanihadesuka.compose.foundation.VerticalScrollbarLayout

@Composable
internal fun ScrollbarLayout(
    orientation: Orientation,
    thumbSizeNormalized: Float,
    thumbOffsetNormalized: Float,
    thumbIsInAction: Boolean,
    thumbIsSelected: Boolean,
    settings: ScrollbarLayoutSettings,
    draggableModifier: Modifier,
    indicator: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    when (orientation) {
        Orientation.Vertical -> VerticalScrollbarLayout(
            thumbSizeNormalized = thumbSizeNormalized,
            thumbOffsetNormalized = thumbOffsetNormalized,
            thumbIsInAction = thumbIsInAction,
            thumbIsSelected = thumbIsSelected,
            settings = settings,
            draggableModifier = draggableModifier,
            indicator = indicator,
            modifier = modifier,
        )

        Orientation.Horizontal -> HorizontalScrollbarLayout(
            thumbSizeNormalized = thumbSizeNormalized,
            thumbOffsetNormalized = thumbOffsetNormalized,
            thumbIsInAction = thumbIsInAction,
            thumbIsSelected = thumbIsSelected,
            settings = settings,
            draggableModifier = draggableModifier,
            indicator = indicator,
            modifier = modifier,
        )
    }
}
