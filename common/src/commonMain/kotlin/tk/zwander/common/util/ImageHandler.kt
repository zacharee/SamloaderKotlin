package tk.zwander.common.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
expect fun imageResource(path: String): ImageBitmap

@Composable
expect fun vectorResource(path: String): ImageVector