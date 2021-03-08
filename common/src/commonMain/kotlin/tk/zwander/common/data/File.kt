package tk.zwander.common.data

import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream

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
    val name: String
    val parent: String
    val parentFile: IPlatformFile
    val path: String
    val isAbsolute: Boolean
    val absolutePath: String
    val absoluteFile: IPlatformFile
    val canonicalPath: String
    val canonicalFile: IPlatformFile
    val canRead: Boolean
    val canWrite: Boolean
    val exists: Boolean
    val isDirectory: Boolean
    val isFile: Boolean
    val isHidden: Boolean
    val lastModified: Long
    val length: Long
    val totalSpace: Long
    val freeSpace: Long
    val usableSpace: Long

    fun createNewFile(): Boolean
    fun delete(): Boolean
    fun deleteOnExit()
    fun list(): Array<String>?
    fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>?
    fun listFiles(): Array<IPlatformFile>?
    fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>?
    fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>?
    fun mkdir(): Boolean
    fun mkdirs(): Boolean
    fun renameTo(dest: File): Boolean
    fun setLastModified(time: Long): Boolean
    fun setReadOnly(): Boolean
    fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean
    fun setWritable(writable: Boolean): Boolean
    fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean
    fun setReadable(readable: Boolean): Boolean
    fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean
    fun setExecutable(executable: Boolean): Boolean
    fun canExecute(): Boolean

    fun openOutputStream(append: Boolean = false): AsyncOutputStream
    fun openInputStream(): AsyncInputStream

    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
}