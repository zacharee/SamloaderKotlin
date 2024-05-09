package tk.zwander.commonCompose.util

import androidx.compose.ui.graphics.Color

fun Color.toAwtColor(): java.awt.Color {
    return java.awt.Color(red, green, blue, alpha)
}
