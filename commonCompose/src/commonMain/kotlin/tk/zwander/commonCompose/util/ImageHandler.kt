package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import dev.icerock.moko.resources.AssetResource

/**
 * Handle retrieving images and vector images from the platform.
 */

@Composable
expect fun vectorResource(resource: AssetResource): Painter
