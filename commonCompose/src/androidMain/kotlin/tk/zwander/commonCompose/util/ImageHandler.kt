package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import dev.icerock.moko.resources.AssetResource

@Composable
actual fun vectorResource(resource: AssetResource, result: (Painter) -> Unit) {
    val context = LocalContext.current
    val density = LocalDensity.current

    result(loadSvgPainter(context.assets.open(resource.path).readBytes(), density))
}
