package tk.zwander.common.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import org.xml.sax.InputSource
import tk.zwander.common.util.jetbrains.parseVectorRoot
import tk.zwander.samsungfirmwaredownloader.App
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

@Composable
actual fun imageResource(path: String): ImageBitmap {
    return imageFromResource(path).asImageBitmap()
}

@Composable
actual fun vectorResource(path: String): ImageVector {
    return DocumentBuilderFactory.newInstance()
        .apply {
            isNamespaceAware = true
        }
        .newDocumentBuilder()
        .parse(object : InputSource() {
            override fun getByteStream(): InputStream {
                return Thread.currentThread().contextClassLoader!!
                    .getResourceAsStream(path)
            }
        })
        .documentElement
        .parseVectorRoot(LocalDensity.current)
}

fun imageFromResource(path: String): Bitmap {
    return BitmapFactory.decodeResourceStream(
        null, null,
        Thread.currentThread().contextClassLoader!!
            .getResourceAsStream(path),
        null, null
    )
}

fun vectorFromResource(path: String): VectorDrawable {
    return VectorDrawableCompat.createFromResourceStream(
        null, null,
        Thread.currentThread().contextClassLoader!!
            .getResourceAsStream(path),
        null, null
    ) as VectorDrawable
}