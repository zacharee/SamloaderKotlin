package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Handle retrieving images and vector images from the platform.
 */
@Composable
actual fun imageResource(path: String): ImageBitmap {
    return ImageBitmap(0, 0)
}

@Composable
actual fun vectorResource(path: String): ImageVector {
    return ImageVector.Builder(defaultWidth = 0.dp, defaultHeight = 0.dp, viewportWidth = 0f, viewportHeight = 0f).build()
}
