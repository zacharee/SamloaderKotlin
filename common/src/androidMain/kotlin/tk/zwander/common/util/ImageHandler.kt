package tk.zwander.common.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource

@Composable
actual fun imageResource(path: String): ImageBitmap {
    val ctx = LocalContext.current
    val res = ctx.resources
    val idForName = res.getIdentifier(path, "drawable", ctx.packageName)

    return ImageBitmap.imageResource(idForName)
}

@Composable
actual fun vectorResource(path: String): ImageVector {
    val ctx = LocalContext.current
    val res = ctx.resources
    val idForName = res.getIdentifier(path, "drawable", ctx.packageName)

    return ImageVector.vectorResource(idForName)
}