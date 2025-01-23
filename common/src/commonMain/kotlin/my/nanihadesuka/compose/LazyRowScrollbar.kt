package my.nanihadesuka.compose

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import my.nanihadesuka.compose.controller.rememberLazyListStateController
import my.nanihadesuka.compose.generic.ElementScrollbar

@Composable
fun LazyRowScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    settings: ScrollbarSettings = ScrollbarSettings.Default,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (!settings.enabled) content()
    else Box(modifier) {
        content()
        InternalLazyRowScrollbar(
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
fun InternalLazyRowScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    settings: ScrollbarSettings = ScrollbarSettings.Default,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
) {
    val controller = rememberLazyListStateController(
        state = state,
        thumbMinLength = settings.thumbMinLength,
        thumbMaxLength = settings.thumbMaxLength,
        alwaysShowScrollBar = settings.alwaysShowScrollbar,
        selectionMode = settings.selectionMode,
    )

    ElementScrollbar(
        orientation = Orientation.Horizontal,
        stateController = controller,
        modifier = modifier,
        settings = settings,
        indicatorContent = indicatorContent,
    )
}
