package tk.zwander.common.data

import korlibs.io.stream.AsyncInputStream
import korlibs.io.stream.AsyncOutputStream
import korlibs.io.stream.toAsync
import tk.zwander.common.util.flushingAsync
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * A File implementation that wraps Java's File class.
 */
actual open class PlatformFile : File {
    private val wrappedFile: java.io.File

    actual constructor(pathName: String) {
        wrappedFile = java.io.File(pathName)
    }

    actual constructor(parent: String, child: String) {
        wrappedFile = java.io.File(parent, child)
    }

    actual constructor(parent: File, child: String) {
        wrappedFile = java.io.File(java.io.File(parent.getAbsolutePath()), child)
    }

    constructor(parent: java.io.File, child: String) {
        wrappedFile = java.io.File(parent, child)
    }

    constructor(file: java.io.File) {
        wrappedFile = file
    }

    override fun getName(): String = wrappedFile.name
    override suspend fun getParent(): String? = wrappedFile.parent
    override suspend fun getParentFile(): IPlatformFile? = File(wrappedFile.parentFile.absolutePath)
    override fun getPath(): String = wrappedFile.path
    override suspend fun isAbsolute(): Boolean = wrappedFile.isAbsolute
    override fun getAbsolutePath(): String = wrappedFile.absolutePath
    override fun getAbsoluteFile(): IPlatformFile = File(wrappedFile.absoluteFile.absolutePath)
    override suspend fun getCanonicalPath(): String = wrappedFile.canonicalPath
    override suspend fun getCanonicalFile(): IPlatformFile = File(wrappedFile.canonicalFile.absolutePath)
    override suspend fun getCanRead(): Boolean = wrappedFile.canRead()
    override suspend fun getCanWrite(): Boolean = wrappedFile.canWrite()
    override suspend fun getExists(): Boolean = wrappedFile.exists()
    override suspend fun isDirectory(): Boolean = wrappedFile.isDirectory
    override suspend fun isFile(): Boolean = wrappedFile.isFile
    override suspend fun isHidden(): Boolean = wrappedFile.isHidden
    override suspend fun getLastModified(): Long = wrappedFile.lastModified()
    override suspend fun getLength(): Long = wrappedFile.length()
    override suspend fun getTotalSpace(): Long = wrappedFile.totalSpace
    override suspend fun getFreeSpace(): Long = wrappedFile.freeSpace
    override suspend fun getUsableSpace(): Long = wrappedFile.usableSpace

    override suspend fun createNewFile(): Boolean {
        return wrappedFile.createNewFile()
    }

    override suspend fun delete(): Boolean {
        return wrappedFile.delete()
    }

    override suspend fun deleteOnExit() {
        wrappedFile.deleteOnExit()
    }

    override suspend fun list(): Array<String>? {
        return wrappedFile.list()
    }

    override suspend fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>? {
        return wrappedFile.list { dir, name -> filter(File(dir.absolutePath), name) }
    }

    override suspend fun listFiles(): Array<IPlatformFile>? {
        return wrappedFile.listFiles()?.map { File(it.absolutePath) }
            ?.toTypedArray()
    }

    override suspend fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>? {
        return wrappedFile.listFiles { dir, name -> filter(File(dir.absolutePath), name) }
            ?.map { File(it.absolutePath) }
            ?.toTypedArray()
    }

    override suspend fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>? {
        return wrappedFile.listFiles { pathname -> filter(File(pathname.absolutePath)) }
            ?.map { File(it.absolutePath) }
            ?.toTypedArray()
    }

    override suspend fun mkdir(): Boolean {
        return wrappedFile.mkdir()
    }

    override suspend fun mkdirs(): Boolean {
        return wrappedFile.mkdirs()
    }

    override suspend fun renameTo(dest: File): Boolean {
        return wrappedFile.renameTo(java.io.File(dest.getAbsolutePath()))
    }

    override suspend fun setLastModified(time: Long): Boolean {
        return wrappedFile.setLastModified(time)
    }

    override suspend fun setReadOnly(): Boolean {
        return wrappedFile.setReadOnly()
    }

    override suspend fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        return wrappedFile.setWritable(writable, ownerOnly)
    }

    override suspend fun setWritable(writable: Boolean): Boolean {
        return wrappedFile.setWritable(writable)
    }

    override suspend fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        return wrappedFile.setReadable(readable, ownerOnly)
    }

    override suspend fun setReadable(readable: Boolean): Boolean {
        return wrappedFile.setReadable(readable)
    }

    override suspend fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        return wrappedFile.setExecutable(executable, ownerOnly)
    }

    override suspend fun setExecutable(executable: Boolean): Boolean {
        return wrappedFile.setExecutable(executable)
    }

    override suspend fun canExecute(): Boolean {
        return wrappedFile.canExecute()
    }

    override suspend fun openOutputStream(append: Boolean): AsyncOutputStream {
        return FileOutputStream(wrappedFile, append).flushingAsync()
    }

    override suspend fun openInputStream(): AsyncInputStream {
        return FileInputStream(wrappedFile).toAsync()
    }

    override fun hashCode(): Int {
        return wrappedFile.hashCode()
    }

    override fun compareTo(other: IPlatformFile): Int {
        return wrappedFile.compareTo(java.io.File(other.getAbsolutePath()))
    }

    override fun equals(other: Any?): Boolean {
        return other is PlatformFile && wrappedFile.absolutePath == other.getAbsolutePath()
    }
}
