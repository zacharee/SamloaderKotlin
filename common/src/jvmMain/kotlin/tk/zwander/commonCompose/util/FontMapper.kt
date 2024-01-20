@file:JvmName("FontMapperJVM")
package tk.zwander.commonCompose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.ui.text.platform.FontLoadResult
import korlibs.memory.Platform
import tk.zwander.common.util.CrossPlatformBugsnag

actual object FontMapper {
    private val GenericFontFamiliesMapping: Map<String, List<String>> by lazy {
        when  {
            Platform.isLinux ->
                mapOf(
                    FontFamily.SansSerif.name to listOf("Noto Sans", "DejaVu Sans", "Arial"),
                    FontFamily.Serif.name to listOf("Noto Serif", "DejaVu Serif", "Times New Roman"),
                    FontFamily.Monospace.name to listOf("Noto Sans Mono", "DejaVu Sans Mono", "Consolas"),
                    // better alternative?
                    FontFamily.Cursive.name to listOf("Comic Sans MS"),
                )
            Platform.isWindows ->
                mapOf(
                    // Segoe UI is the Windows system font, so try it first.
                    // See https://learn.microsoft.com/en-us/windows/win32/uxguide/vis-fonts
                    FontFamily.SansSerif.name to listOf("Eeby Deeby", "Segoe UI", "Arial"),
                    FontFamily.Serif.name to listOf("Eeby Deeby", "Times New Roman"),
                    FontFamily.Monospace.name to listOf("Consolas"),
                    FontFamily.Cursive.name to listOf("Comic Sans MS"),
                )
            Platform.isMac || Platform.isIos || Platform.isTvos || Platform.isWatchos ->
                mapOf(
                    // .AppleSystem* aliases is the only legal way to get default SF and NY fonts.
                    FontFamily.SansSerif.name to listOf(".AppleSystemUIFont", "Helvetica Neue", "Helvetica"),
                    FontFamily.Serif.name to listOf(".AppleSystemUIFontSerif", "Times", "Times New Roman"),
                    FontFamily.Monospace.name to listOf(".AppleSystemUIFontMonospaced", "Menlo", "Courier"),
                    // Safari "font-family: cursive" real font names from macOS and iOS.
                    FontFamily.Cursive.name to listOf("Apple Chancery", "Snell Roundhand"),
                )
            Platform.isAndroid -> // https://m3.material.io/styles/typography/fonts
                mapOf(
                    FontFamily.SansSerif.name to listOf("Roboto", "Noto Sans"),
                    FontFamily.Serif.name to listOf("Roboto Serif", "Noto Serif"),
                    FontFamily.Monospace.name to listOf("Roboto Mono", "Noto Sans Mono"),
                    FontFamily.Cursive.name to listOf("Comic Sans MS"),
                )
            else ->
                mapOf(
                    FontFamily.SansSerif.name to listOf("Arial"),
                    FontFamily.Serif.name to listOf("Times New Roman"),
                    FontFamily.Monospace.name to listOf("Consolas"),
                    FontFamily.Cursive.name to listOf("Comic Sans MS"),
                )
        }
    }

    @OptIn(ExperimentalTextApi::class)
    @Composable
    actual fun mapGenericFontFamilyToSpecificFontFamily(family: FontFamily): FontFamily {
        if (family !is GenericFontFamily) {
            return family
        }

        val mapped = GenericFontFamiliesMapping[family.name] ?: return family
        val resolver = LocalFontFamilyResolver.current

        return mapped.firstNotNullOfOrNull { fontName ->
            try {
                FontFamily(fontName).also { mappedFamily ->
                    resolver.resolve(mappedFamily).also { resolved ->
                        if ((resolved.value as? FontLoadResult)?.typeface?.familyName != fontName) {
                            return@firstNotNullOfOrNull null
                        }
                    }
                }
            } catch (e: Throwable) {
                CrossPlatformBugsnag.notify(Exception("Unable to load font $fontName.", e))
                e.printStackTrace()
                null
            }
        } ?: family
    }
}