package tk.zwander.commonCompose.util

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

actual fun String.asClipEntry(): ClipEntry = ClipEntry(ClipData.newPlainText(null, this))
