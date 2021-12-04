package tk.zwander.common.data

sealed class FetchResult(
    val error: Exception? = null,
    val rawOutput: String = ""
) {
    class VersionFetchResult(
        val versionCode: String = "",
        val androidVersion: String = "",
        error: Exception? = null,
        rawOutput: String = ""
    ) : FetchResult(error, rawOutput) {
        operator fun component1() = versionCode
        operator fun component2() = androidVersion
        operator fun component3() = error
        operator fun component4() = rawOutput
    }

    class GetBinaryFileResult(
        val info: BinaryFileInfo? = null,
        error: Exception? = null,
        rawOutput: String = ""
    ) : FetchResult(error, rawOutput) {
        operator fun component1() = info
        operator fun component2() = error
        operator fun component3() = rawOutput
    }
}
