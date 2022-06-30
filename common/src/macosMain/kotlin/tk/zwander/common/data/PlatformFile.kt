package tk.zwander.common.data

import com.soywiz.klock.DateTime
import com.soywiz.korio.file.*
import com.soywiz.korio.file.std.rootLocalVfsNative
import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream

/**
 * A File implementation that wraps macOS' file API.
 */
actual open class PlatformFile : File {
    private val wrappedFile: VfsFile

    actual constructor(pathName: String) {
        wrappedFile = VfsFile(rootLocalVfsNative, pathName)
    }

    actual constructor(parent: String, child: String) {
        wrappedFile = VfsFile(rootLocalVfsNative, "$parent/$child")
    }

    actual constructor(parent: File, child: String) {
        wrappedFile = VfsFile(rootLocalVfsNative, "${parent.getAbsolutePath()}/$child")
    }

    constructor(parent: VfsFile, child: String) {
        wrappedFile = VfsFile(rootLocalVfsNative, "${parent.absolutePath}/$child")
    }

    constructor(file: VfsFile) {
        wrappedFile = file
    }

    override fun getName(): String = wrappedFile.baseName
    override suspend fun getParent(): String = wrappedFile.parent.absolutePath
    override suspend fun getParentFile(): File = File(wrappedFile.parent.absolutePath)
    override fun getPath(): String = wrappedFile.path
    override suspend fun isAbsolute(): Boolean = wrappedFile.absolutePathInfo.isAbsolute()
    override fun getAbsolutePath(): String = wrappedFile.absolutePath
    override fun getAbsoluteFile(): File = File(wrappedFile.absolutePath)
    override suspend fun getCanonicalPath(): String = wrappedFile.absolutePath
    override suspend fun getCanonicalFile(): File = File(wrappedFile.absolutePath)
    override suspend fun getCanRead(): Boolean = true
    override suspend fun getCanWrite(): Boolean = true
    override suspend fun getExists(): Boolean = wrappedFile.exists()
    override suspend fun isDirectory(): Boolean = wrappedFile.isDirectory()
    override suspend fun isFile(): Boolean = wrappedFile.isFile()
    override suspend fun isHidden(): Boolean = false
    override suspend fun getLastModified(): Long = wrappedFile.stat().modifiedTime.unixMillisLong
    override suspend fun getLength(): Long = wrappedFile.size()
    override suspend fun getTotalSpace(): Long = 0
    override suspend fun getFreeSpace(): Long = 0
    override suspend fun getUsableSpace(): Long = 0

    override suspend fun createNewFile(): Boolean {
        return try {
            wrappedFile.touch(DateTime.now())
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun delete(): Boolean {
        return wrappedFile.delete()
    }

    override suspend fun deleteOnExit() {

    }

    override suspend fun list(): Array<String>? {
        return wrappedFile.listNames().toTypedArray()
    }

    override suspend fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>? {
        return wrappedFile.listSimple().filter { filter(File(wrappedFile.absolutePath), it.fullName) }.map { it.fullName }.toTypedArray()
    }

    override suspend fun listFiles(): Array<IPlatformFile>? {
        return wrappedFile.listSimple().map { File(it.absolutePath) }
            .toTypedArray()
    }

    override suspend fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>? {
        return wrappedFile.listSimple().filter { filter(File(wrappedFile.absolutePath), it.fullName) }.map { File(it.absolutePath) }.toTypedArray()
    }

    override suspend fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>? {
        return wrappedFile.listSimple().filter { filter(File(it.absolutePath)) }.map { File(it.absolutePath) }.toTypedArray()
    }

    override suspend fun mkdir(): Boolean {
        return wrappedFile.mkdir()
    }

    override suspend fun mkdirs(): Boolean {
        return wrappedFile.mkdirs()
    }

    override suspend fun renameTo(dest: File): Boolean {
        return wrappedFile.renameTo(dest.getAbsolutePath())
    }

    override suspend fun setLastModified(time: Long): Boolean {
        return false
    }

    override suspend fun setReadOnly(): Boolean {
        return false
    }

    override suspend fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        return false
    }

    override suspend fun setWritable(writable: Boolean): Boolean {
        return false
    }

    override suspend fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        return false
    }

    override suspend fun setReadable(readable: Boolean): Boolean {
        return false
    }

    override suspend fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        return false
    }

    override suspend fun setExecutable(executable: Boolean): Boolean {
        return false
    }

    override suspend fun canExecute(): Boolean {
        return true
    }

    override suspend fun openOutputStream(append: Boolean): AsyncOutputStream {
        return wrappedFile.open(if (append) VfsOpenMode.APPEND else VfsOpenMode.CREATE)
    }

    override suspend fun openInputStream(): AsyncInputStream {
        return wrappedFile.openInputStream()
    }

    override fun hashCode(): Int {
        return wrappedFile.hashCode()
    }

    override fun compareTo(other: IPlatformFile): Int {
        return wrappedFile.absolutePath.compareTo(other.getAbsolutePath())
    }

    override fun equals(other: Any?): Boolean {
        return other is PlatformFile && wrappedFile.absolutePath == other.getAbsolutePath()
    }
}
