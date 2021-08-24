package tk.zwander.common.data

import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream
import kotlinx.coroutines.CoroutineScope

/**
 * Platforms should actuate this class to implement
 * various filesystem classes they use.
 */
expect open class PlatformFile : File {
    constructor(pathName: String)
    constructor(parent: String, child: String)
    constructor(parent: File, child: String)
}

/**
 * A "File" representation for Kotlin MPP, to
 * allow easier interaction with platform filesystem
 * stuff.
 */
abstract class File : IPlatformFile {
    companion object {
        operator fun invoke(pathName: String): File {
            return PlatformFile(pathName)
        }

        operator fun invoke(parent: String, child: String): File {
            return PlatformFile(parent, child)
        }

        operator fun invoke(parent: File, child: String): File {
            return PlatformFile(parent, child)
        }
    }
}

/**
 * The base File representation for Platform Files to
 * override if needed.
 */
interface IPlatformFile : Comparable<IPlatformFile> {
    fun getName(): String
    suspend fun getParent(): String?
    suspend fun getParentFile(): IPlatformFile?
    fun getPath(): String
    suspend fun isAbsolute(): Boolean
    fun getAbsolutePath(): String
    fun getAbsoluteFile(): IPlatformFile
    suspend fun getCanonicalPath(): String
    suspend fun getCanonicalFile(): IPlatformFile
    suspend fun getCanRead(): Boolean
    suspend fun getCanWrite(): Boolean
    suspend fun getExists(): Boolean
    suspend fun isDirectory(): Boolean
    suspend fun isFile(): Boolean
    suspend fun isHidden(): Boolean
    suspend fun getLastModified(): Long
    suspend fun getLength(): Long
    suspend fun getTotalSpace(): Long
    suspend fun getFreeSpace(): Long
    suspend fun getUsableSpace(): Long

    suspend fun createNewFile(): Boolean
    suspend fun delete(): Boolean
    suspend fun deleteOnExit()
    suspend fun list(): Array<String>?
    suspend fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>?
    suspend fun listFiles(): Array<IPlatformFile>?
    suspend fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>?
    suspend fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>?
    suspend fun mkdir(): Boolean
    suspend fun mkdirs(): Boolean
    suspend fun renameTo(dest: File): Boolean
    suspend fun setLastModified(time: Long): Boolean
    suspend fun setReadOnly(): Boolean
    suspend fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean
    suspend fun setWritable(writable: Boolean): Boolean
    suspend fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean
    suspend fun setReadable(readable: Boolean): Boolean
    suspend fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean
    suspend fun setExecutable(executable: Boolean): Boolean
    suspend fun canExecute(): Boolean

    suspend fun openOutputStream(append: Boolean = false): AsyncOutputStream
    suspend fun openInputStream(): AsyncInputStream

    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
}
