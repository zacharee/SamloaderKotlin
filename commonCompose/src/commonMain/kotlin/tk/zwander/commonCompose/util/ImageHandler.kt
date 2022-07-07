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
fun vectorResource(resource: AssetResource): Painter {
    var painter by remember(resource) { mutableStateOf<Painter?>(null) }

    vectorResource(resource) {
        painter = it
    }

    return painter ?: ColorPainter(Color.Transparent)
}

@Composable
expect fun vectorResource(resource: AssetResource, result: (Painter) -> Unit)
