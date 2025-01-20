package tk.zwander.commonCompose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.launch
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

@Composable
fun keyCodeHandler(): (KeyEvent) -> Boolean {
    val scope = rememberCoroutineScope()
    val pagerState = LocalPagerState.current

    return { keyEvent ->
        val isModifierPressed = if (hostOs == OS.MacOS) keyEvent.isCtrlPressed else keyEvent.isAltPressed

        if (isModifierPressed && keyEvent.type == KeyEventType.KeyDown) {
            val multiplier = when (keyEvent.key) {
                Key.DirectionRight -> 1
                Key.DirectionLeft -> -1
                else -> 0
            }

            val newIndex = pagerState.currentPage + multiplier

            if (newIndex == pagerState.currentPage) {
                false
            } else {
                scope.launch {
                    pagerState.animateScrollToPage(newIndex)
                }
                true
            }
        } else {
            false
        }
    }
}
