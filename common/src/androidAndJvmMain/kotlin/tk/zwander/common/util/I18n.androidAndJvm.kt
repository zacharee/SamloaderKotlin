package tk.zwander.common.util

import java.util.Locale

actual object I18n {
    actual fun getCountryNameForCode(code: String): String? {
        return Locale(androidx.compose.ui.text.intl.Locale.current.language, code).displayCountry
    }
}