@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")

package tk.zwander.commonCompose.util

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density

internal expect fun loadSvgPainter(
    input: ByteArray,
    density: Density
): Painter
