package tk.zwander.commonCompose.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

@OptIn(ExperimentalComposeUiApi::class)
actual fun String.asClipEntry(): ClipEntry = ClipEntry.withPlainText(this)
