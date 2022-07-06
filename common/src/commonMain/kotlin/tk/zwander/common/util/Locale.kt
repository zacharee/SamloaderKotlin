package tk.zwander.common.util

import de.comahe.i18n4k.Locale
import de.comahe.i18n4k.config.I18n4kConfigDefault
import de.comahe.i18n4k.i18n4k

fun initLocale() {
    val config = I18n4kConfigDefault()
    config.locale = locale

    i18n4k = config
}

expect val locale: Locale
