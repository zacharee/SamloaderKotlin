package tk.zwander.commonCompose.util

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import dev.icerock.moko.resources.AssetResource

/**
 * Handle retrieving images and vector images from the platform.
 */

@Composable
internal fun vectorResource(resource: AssetResource): Painter {
    var painter by remember(resource) { mutableStateOf<Painter?>(null) }

    vectorResourceImpl(resource) {
        painter = it
    }

    return painter ?: ColorPainter(Color.Transparent)
}

@Composable
expect fun vectorResourceImpl(resource: AssetResource, result: (Painter) -> Unit)
