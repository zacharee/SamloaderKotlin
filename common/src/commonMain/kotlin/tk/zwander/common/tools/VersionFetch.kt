package tk.zwander.common.tools

expect object VersionFetch {
    suspend fun getLatestVer(model: String, region: String): String
}