package tk.zwander.common.data

import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream
import com.soywiz.korio.stream.toAsync
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
        wrappedFile = java.io.File(java.io.File(parent.absolutePath), child)
    }

    constructor(parent: java.io.File, child: String) {
        wrappedFile = java.io.File(parent, child)
    }

    constructor(file: java.io.File) {
        wrappedFile = file
    }

    override val name: String
        get() = wrappedFile.name
    override val parent: String
        get() = wrappedFile.parent
    override val parentFile: File
        get() = File(wrappedFile.parentFile.absolutePath)
    override val path: String
        get() = wrappedFile.path
    override val isAbsolute: Boolean
        get() = wrappedFile.isAbsolute
    override val absolutePath: String
        get() = wrappedFile.absolutePath
    override val absoluteFile: File
        get() = File(wrappedFile.absoluteFile.absolutePath)
    override val canonicalPath: String
        get() = wrappedFile.canonicalPath
    override val canonicalFile: File
        get() = File(wrappedFile.canonicalFile.absolutePath)
    override val canRead: Boolean
        get() = wrappedFile.canRead()
    override val canWrite: Boolean
        get() = wrappedFile.canWrite()
    override val exists: Boolean
        get() = wrappedFile.exists()
    override val isDirectory: Boolean
        get() = wrappedFile.isDirectory
    override val isFile: Boolean
        get() = wrappedFile.isFile
    override val isHidden: Boolean
        get() = wrappedFile.isHidden
    override val lastModified: Long
        get() = wrappedFile.lastModified()
    override val length: Long
        get() = wrappedFile.length()
    override val totalSpace: Long
        get() = wrappedFile.totalSpace
    override val freeSpace: Long
        get() = wrappedFile.freeSpace
    override val usableSpace: Long
        get() = wrappedFile.usableSpace

    override fun createNewFile(): Boolean {
        return wrappedFile.createNewFile()
    }

    override fun delete(): Boolean {
        return wrappedFile.delete()
    }

    override fun deleteOnExit() {
        wrappedFile.deleteOnExit()
    }

    override fun list(): Array<String>? {
        return wrappedFile.list()
    }

    override fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>? {
        return wrappedFile.list { dir, name -> filter(File(dir.absolutePath), name) }
    }

    override fun listFiles(): Array<IPlatformFile>? {
        return wrappedFile.listFiles()?.map { File(it.absolutePath) }
            ?.toTypedArray()
    }

    override fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>? {
        return wrappedFile.listFiles { dir, name -> filter(File(dir.absolutePath), name) }
            ?.map { File(it.absolutePath) }
            ?.toTypedArray()
    }

    override fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>? {
        return wrappedFile.listFiles { pathname -> filter(File(pathname.absolutePath)) }
            ?.map { File(it.absolutePath) }
            ?.toTypedArray()
    }

    override fun mkdir(): Boolean {
        return wrappedFile.mkdir()
    }

    override fun mkdirs(): Boolean {
        return wrappedFile.mkdirs()
    }

    override fun renameTo(dest: File): Boolean {
        return wrappedFile.renameTo(java.io.File(dest.absolutePath))
    }

    override fun setLastModified(time: Long): Boolean {
        return wrappedFile.setLastModified(time)
    }

    override fun setReadOnly(): Boolean {
        return wrappedFile.setReadOnly()
    }

    override fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        return wrappedFile.setWritable(writable, ownerOnly)
    }

    override fun setWritable(writable: Boolean): Boolean {
        return wrappedFile.setWritable(writable)
    }

    override fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        return wrappedFile.setReadable(readable, ownerOnly)
    }

    override fun setReadable(readable: Boolean): Boolean {
        return wrappedFile.setReadable(readable)
    }

    override fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        return wrappedFile.setExecutable(executable, ownerOnly)
    }

    override fun setExecutable(executable: Boolean): Boolean {
        return wrappedFile.setExecutable(executable)
    }

    override fun canExecute(): Boolean {
        return wrappedFile.canExecute()
    }

    override fun openOutputStream(append: Boolean): AsyncOutputStream {
        return FileOutputStream(wrappedFile, append).flushingAsync()
    }

    override fun openInputStream(): AsyncInputStream {
        return FileInputStream(wrappedFile).toAsync()
    }

    override fun hashCode(): Int {
        return wrappedFile.hashCode()
    }

    override fun compareTo(other: IPlatformFile): Int {
        return wrappedFile.compareTo(java.io.File(other.absolutePath))
    }

    override fun equals(other: Any?): Boolean {
        return other is PlatformFile && wrappedFile.absolutePath == other.absolutePath
    }
}