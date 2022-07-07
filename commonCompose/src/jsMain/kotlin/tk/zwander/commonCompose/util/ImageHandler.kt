package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.AssetResource
import tk.zwander.common.util.runBlocking

/**
 * Handle retrieving images and vector images from the platform.
 */

@Composable
actual fun vectorResource(resource: AssetResource): Painter {
    return loadSvgPainter(runBlocking { resource.getText().encodeToByteArray() }!!, LocalDensity.current)
}
