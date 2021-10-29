package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import org.jetbrains.skia.Image
import org.xml.sax.InputSource

fun loadResource(path: String): ByteArray {
    val resource = Thread.currentThread().contextClassLoader.getResource(path)
    requireNotNull(resource) { "Resource $path not found" }
    return resource.readBytes()
}

@Composable
actual fun imageResource(path: String): ImageBitmap {
    return Image.makeFromEncoded(loadResource(path)).toComposeImageBitmap()
}

@Composable
actual fun vectorResource(path: String): ImageVector {
    return loadXmlImageVector(InputSource(loadResource(path).inputStream()), Density(1.0f))
}
