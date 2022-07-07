package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import com.soywiz.korio.stream.openAsync
import dev.icerock.moko.resources.AssetResource
import org.jetbrains.skia.Image
import org.xml.sax.InputSource

fun loadResource(path: String): ByteArray {
    val resource = Thread.currentThread().contextClassLoader.getResource(path)
    requireNotNull(resource) { "Resource $path not found" }
    return resource.readBytes()
}

@Composable
fun imageResource(path: String): ImageBitmap {
    return Image.makeFromEncoded(loadResource(path)).toComposeImageBitmap()
}

@Composable
actual fun vectorResource(resource: AssetResource, result: (Painter) -> Unit) {
    result(loadSvgPainter(loadResource(resource.filePath), LocalDensity.current))
}
