package tk.zwander.common.util

import de.comahe.i18n4k.Locale

actual val locale: Locale
    get() = Locale.getDefault()
