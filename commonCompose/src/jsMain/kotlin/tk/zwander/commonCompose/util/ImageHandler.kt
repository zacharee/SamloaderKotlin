package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.AssetResource
import tk.zwander.common.util.runBlocking

/**
 * Handle retrieving images and vector images from the platform.
 */

@Composable
actual fun vectorResourceImpl(resource: AssetResource, result: (Painter) -> Unit) {
    val density = LocalDensity.current

    LaunchedEffect(resource) {
        result(loadSvgPainter(resource.getText().encodeToByteArray(), density))
    }
}
