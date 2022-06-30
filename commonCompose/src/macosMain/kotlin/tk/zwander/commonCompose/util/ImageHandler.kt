package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Image

fun loadResource(path: String): ByteArray {
//    val resource = Thread.currentThread().contextClassLoader.getResource(path)
//    requireNotNull(resource) { "Resource $path not found" }
//    return resource.readBytes()
    return ByteArray(0)
}

@Composable
actual fun imageResource(path: String): ImageBitmap {
    return Image.makeFromEncoded(loadResource(path)).toComposeImageBitmap()
}

@Composable
actual fun vectorResource(path: String): ImageVector {
    return ImageVector.Builder(defaultHeight = 0.dp, defaultWidth = 0.dp, viewportHeight = 0f, viewportWidth = 0f).build()
//    return loadXmlImageVector(InputSource(loadResource(path).inputStream()), Density(1.0f))
}
