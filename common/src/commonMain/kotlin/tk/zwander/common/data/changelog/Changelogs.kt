package tk.zwander.common.data.changelog

data class Changelogs(
    val model: String,
    val region: String,
    val changelogs: Map<String, Changelog>
)