package tk.zwander.common.data

/**
 * Represents a binary file to download.
 */
data class BinaryFileInfo(
    val path: String,
    val fileName: String,
    val size: Long,
    val crc32: Long?,
    val v4Key: Pair<ByteArray, String>?,
)
