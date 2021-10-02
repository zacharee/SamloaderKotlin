package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import org.jetbrains.skija.Image

fun loadResource(path: String): ByteArray {
    val resource = Thread.currentThread().contextClassLoader.getResource(path)
    requireNotNull(resource) { "Resource $path not found" }
    return resource.readBytes()
}

@Composable
actual fun imageResource(path: String): ImageBitmap {
    return Image.makeFromEncoded(loadResource(path)).asImageBitmap()
}

@Composable
actual fun vectorResource(path: String): ImageVector {
    return painterResource(path)
}