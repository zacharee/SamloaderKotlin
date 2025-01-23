package my.nanihadesuka.compose

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import my.nanihadesuka.compose.controller.rememberLazyGridStateController
import my.nanihadesuka.compose.generic.ElementScrollbar

@Composable
fun LazyHorizontalGridScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    settings: ScrollbarSettings = ScrollbarSettings.Default,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (!settings.enabled) content()
    else Box(modifier) {
        content()
        InternalLazyHorizontalGridScrollbar(
            state = state,
            settings = settings,
            indicatorContent = indicatorContent,
        )
    }
}

/**
 * Use this variation if you want to place the scrollbar independently of the list position
 */
@Composable
fun InternalLazyHorizontalGridScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    settings: ScrollbarSettings = ScrollbarSettings.Default,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
) {
    val controller = rememberLazyGridStateController(
        state = state,
        thumbMinLength = settings.thumbMinLength,
        thumbMaxLength = settings.thumbMaxLength,
        alwaysShowScrollBar = settings.alwaysShowScrollbar,
        selectionMode = settings.selectionMode,
        orientation = Orientation.Horizontal
    )

    ElementScrollbar(
        orientation = Orientation.Horizontal,
        stateController = controller,
        modifier = modifier,
        settings = settings,
        indicatorContent = indicatorContent
    )
}
