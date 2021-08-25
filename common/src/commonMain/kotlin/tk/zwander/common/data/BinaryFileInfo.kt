package tk.zwander.common.data

/**
 * Represents a binary file to download.
 */
data class BinaryFileInfo(
    val path: String,
    val fileName: String,
    val size: Long,
    val crc32: Long?,
    val v4Key: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        return other is BinaryFileInfo
                && other.path == path
                && other.fileName == fileName
                && other.size == size
                && other.crc32 == crc32
                && ((other.v4Key == null && v4Key == null) || (other.v4Key != null && v4Key != null && other.v4Key.contentEquals(v4Key)))
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + (crc32?.hashCode() ?: 0)
        result = 31 * result + (v4Key?.contentHashCode() ?: 0)
        return result
    }
}
