package tk.zwander.common.data.changelog

data class Changelog(
    val firmware: String?,
    val androidVer: String?,
    val relDate: String?,
    val secPatch: String?,
    val notes: String?
)
