package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity

@Composable
fun rememberIsOverScaledThreshold(parentSize: Int): Boolean {
    return rememberScaledSize(parentSize) >= 400
}

@Composable
fun rememberScaledSize(parentSize: Int): Float {
    val fontScale = LocalDensity.current.fontScale
    val density = LocalDensity.current.density

    return remember(key1 = fontScale, key2 = density, key3 = parentSize) {
        parentSize / (fontScale * density)
    }
}
