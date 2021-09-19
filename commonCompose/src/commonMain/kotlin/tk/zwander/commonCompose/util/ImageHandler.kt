package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Handle retrieving images and vector images from the platform.
 */

@Composable
expect fun imageResource(path: String): ImageBitmap

@Composable
expect fun vectorResource(path: String): ImageVector