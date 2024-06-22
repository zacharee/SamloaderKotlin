package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity

@Composable
internal fun rememberIsOverScaledThreshold(parentSize: Int, threshold: Int = 600): Boolean {
    return rememberScaledSize(parentSize) >= threshold
}

@Composable
internal fun rememberScaledSize(parentSize: Int): Float {
    val fontScale = LocalDensity.current.fontScale
    val density = LocalDensity.current.density

    return rememberSaveable(fontScale, density, parentSize) {
        parentSize / (fontScale * density)
    }
}
