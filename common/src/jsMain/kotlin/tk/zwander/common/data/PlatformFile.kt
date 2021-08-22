package tk.zwander.common.data

import com.soywiz.korio.file.*
import com.soywiz.korio.file.std.applicationDataVfs
import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream
import tk.zwander.common.util.runBlocking

actual open class PlatformFile : File {
    private val wrappedFile: VfsFile

    actual constructor(pathName: String) {
        wrappedFile = VfsFile(applicationDataVfs.vfs, pathName)
    }

    actual constructor(parent: String, child: String) {
        wrappedFile = VfsFile(applicationDataVfs.vfs, parent.pathInfo.combine(child.pathInfo).fullPath)
    }

    actual constructor(parent: File, child: String) {
        wrappedFile = VfsFile(applicationDataVfs.vfs, parent.absolutePath.pathInfo.combine(child.pathInfo).fullPath)
    }

    constructor(parent: VfsFile, child: String) : this(parent.absolutePath, child)

    constructor(file: VfsFile) {
        wrappedFile = file
    }

    override val name: String
        get() = wrappedFile.baseName
    override val parent: String
        get() = wrappedFile.parent.absolutePath
    override val parentFile: IPlatformFile
        get() = File(parent)
    override val path: String
        get() = wrappedFile.path
    override val isAbsolute: Boolean
        get() = true
    override val absolutePath: String
        get() = wrappedFile.absolutePath
    override val absoluteFile: IPlatformFile
        get() = File(absolutePath)
    override val canonicalPath: String
        get() = absolutePath
    override val canonicalFile: IPlatformFile
        get() = absoluteFile
    override val canRead: Boolean
        get() = true
    override val canWrite: Boolean
        get() = true
    override val exists: Boolean
        get() = runBlocking { wrappedFile.exists() }!!
    override val isDirectory: Boolean
        get() = runBlocking { wrappedFile.isDirectory() }!!
    override val isFile: Boolean
        get() = runBlocking { wrappedFile.isFile() }!!
    override val isHidden: Boolean
        get() = false
    override val lastModified: Long
        get() = runBlocking { wrappedFile.stat().modifiedTime.unixMillisLong }!!
    override val length: Long
        get() = runBlocking { wrappedFile.size() }!!
    override val totalSpace: Long
        get() = throw IllegalStateException("Not implemented!")
    override val freeSpace: Long
        get() = throw IllegalStateException("Not implemented!")
    override val usableSpace: Long
        get() = throw IllegalStateException("Not implemented!")

    override fun createNewFile(): Boolean {
        throw IllegalStateException("Not implemented!")
    }

    override fun delete(): Boolean {
        return runBlocking {
            wrappedFile.delete()
        }!!
    }

    override fun deleteOnExit() {
        throw IllegalStateException("Not implemented!")
    }

    override fun list(): Array<String>? {
        return runBlocking {
            wrappedFile.listSimple().map { it.absolutePath }.toTypedArray()
        }
    }

    override fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>? {
        return runBlocking {
            wrappedFile.listRecursiveSimple { filter(File(it.parent.absolutePath), it.baseName) }
                .map { it.absolutePath }.toTypedArray()
        }
    }

    override fun listFiles(): Array<IPlatformFile>? {
        return list()?.map { File(it) }?.toTypedArray()
    }

    override fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>? {
        return list(filter)?.map { File(it) }?.toTypedArray()
    }

    override fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>? {
        return runBlocking {
            wrappedFile.listRecursiveSimple { filter(File(it.absolutePath)) }
                .map { File(it.absolutePath) }.toTypedArray()
        }
    }

    override fun mkdir(): Boolean {
        return runBlocking {
            wrappedFile.mkdir()
        }!!
    }

    override fun mkdirs(): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override fun renameTo(dest: File): Boolean {
        return runBlocking { wrappedFile.renameTo(dest.absolutePath) }!!
    }

    override fun setLastModified(time: Long): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override fun setReadOnly(): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override fun setWritable(writable: Boolean): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override fun setReadable(readable: Boolean): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override fun setExecutable(executable: Boolean): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override fun canExecute(): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override fun openOutputStream(append: Boolean): AsyncOutputStream {
        return runBlocking {
            wrappedFile.open(if (append) VfsOpenMode.APPEND else VfsOpenMode.CREATE_NEW)
        }!!
    }

    override fun openInputStream(): AsyncInputStream {
        return runBlocking {
            wrappedFile.open(VfsOpenMode.READ)
        }!!
    }

    override fun hashCode(): Int {
        return runBlocking {
            wrappedFile.hashCode()
        }!!
    }

    override fun equals(other: Any?): Boolean {
        return other is IPlatformFile && other.absolutePath == absolutePath
    }

    override fun compareTo(other: IPlatformFile): Int {
        return name.compareTo(other.name)
    }
}