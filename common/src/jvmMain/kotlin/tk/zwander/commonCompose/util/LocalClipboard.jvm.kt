package tk.zwander.commonCompose.util

import androidx.compose.ui.platform.ClipEntry
import java.awt.datatransfer.StringSelection

actual fun String.asClipEntry(): ClipEntry = ClipEntry(StringSelection(this))
