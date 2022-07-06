package tk.zwander.common.util

import de.comahe.i18n4k.Locale
import kotlinx.browser.window

actual val locale: Locale
    get() = window.navigator.language.let {
        val split = it.split("-").toMutableList()
        if (split.size > 1) split[1] = split[1].uppercase()

        Locale(split.joinToString("_"))
    }
