package tk.zwander.commonCompose.view

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Velocity
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Composable
fun Modifier.keyboardDismissalNestedScrolling(
    state: ScrollableState,
): Modifier = composed {
    val keyboardController = LocalSoftwareKeyboardController.current

    then(
        if (keyboardController != null) {
            nestedScroll(
                remember {
                    object : NestedScrollConnection {
                        override suspend fun onPreFling(available: Velocity): Velocity {
                            if (available.y > 0) {
                                keyboardController.hide()
                            }

                            return super.onPreFling(available)
                        }
                    }
                },
            ).pointerInput(null) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 0 && !state.canScrollBackward && !state.canScrollForward) {
                        keyboardController.hide()
                    }
                }
            }
        } else {
            Modifier
        }
    )
}
