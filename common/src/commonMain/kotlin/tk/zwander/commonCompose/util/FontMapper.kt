package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

expect object FontMapper {
    @Composable
    fun mapGenericFontFamilyToSpecificFontFamily(family: FontFamily): FontFamily
}
