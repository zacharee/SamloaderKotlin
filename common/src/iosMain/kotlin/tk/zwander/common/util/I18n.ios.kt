package tk.zwander.common.util

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localizedStringForCountryCode

actual object I18n {
    actual fun getCountryNameForCode(code: String): String? {
        return NSLocale.currentLocale.localizedStringForCountryCode(code)
    }
}