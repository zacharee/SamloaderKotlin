package tk.zwander.common.util

expect object I18n {
    fun getCountryNameForCode(code: String): String?
}
