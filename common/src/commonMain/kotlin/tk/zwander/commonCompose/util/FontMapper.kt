package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.GenericFontFamily
import korlibs.memory.Platform
import tk.zwander.common.util.CrossPlatformBugsnag

expect object FontMapper {
    @Composable
    fun mapGenericFontFamilyToSpecificFontFamily(family: FontFamily): FontFamily
}
