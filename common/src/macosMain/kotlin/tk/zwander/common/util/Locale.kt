package tk.zwander.common.util

import de.comahe.i18n4k.Locale
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual val locale: Locale
    get() = Locale(NSLocale.currentLocale.languageCode.replace("-", "_"))
