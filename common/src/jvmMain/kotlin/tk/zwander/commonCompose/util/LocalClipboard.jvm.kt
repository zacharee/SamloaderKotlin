package tk.zwander.commonCompose.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalComposeUiApi::class)
actual fun String.asClipEntry(): ClipEntry = ClipEntry(StringSelection(this))
