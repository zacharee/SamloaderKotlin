package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

actual object FontMapper {
    @Composable
    actual fun mapGenericFontFamilyToSpecificFontFamily(family: FontFamily): FontFamily {
        return family
    }
}