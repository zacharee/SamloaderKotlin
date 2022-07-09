package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.AssetResource
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import org.jetbrains.skia.Image
import platform.Foundation.NSError

@Composable
actual fun vectorResourceImpl(resource: AssetResource, result: (Painter) -> Unit) {
    result(loadSvgPainter(resource.readText().encodeToByteArray(), LocalDensity.current))
}
