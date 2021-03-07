package tk.zwander.common.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import org.xml.sax.InputSource
import tk.zwander.common.util.jetbrains.parseVectorRoot
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

//Non-functional because of how Android works
//fun Context.vectorFromResource(path: String): Drawable {
//    return VectorDrawableCompat.createFromXml(
//        null,
//        XmlPullParserFactory
//            .newInstance()
//            .apply {
//                this.isNamespaceAware = true
//            }
//            .newPullParser()
//            .apply {
//                setInput(
//                    Thread.currentThread().contextClassLoader!!
//                        .getResourceAsStream(path).reader()
//                )
//            })
//}