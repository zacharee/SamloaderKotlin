package tk.zwander.commonCompose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import korlibs.io.async.launch
import korlibs.memory.Platform

@Composable
fun keyCodeHandler(): (KeyEvent) -> Boolean {
    val scope = rememberCoroutineScope()
    val pagerState = LocalPagerState.current

    return { keyEvent ->
        val nativeEvent = keyEvent.nativeKeyEvent as java.awt.event.KeyEvent
        val isModifierPressed = if (Platform.isMac) keyEvent.isCtrlPressed else keyEvent.isAltPressed

        if (isModifierPressed && nativeEvent.id == java.awt.event.KeyEvent.KEY_PRESSED) {
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
