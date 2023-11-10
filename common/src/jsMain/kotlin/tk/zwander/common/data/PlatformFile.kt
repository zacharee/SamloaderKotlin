package tk.zwander.common.data

import korlibs.io.file.VfsFile
import korlibs.io.file.VfsOpenMode
import korlibs.io.file.baseName
import korlibs.io.file.combine
import korlibs.io.file.pathInfo
import korlibs.io.jsRuntime
import korlibs.io.stream.AsyncInputStream
import korlibs.io.stream.AsyncOutputStream

actual open class PlatformFile : File {
    private val wrappedFile: VfsFile

    actual constructor(pathName: String) {
        wrappedFile = VfsFile(jsRuntime.localStorage().vfs, pathName)
    }

    actual constructor(parent: String, child: String) {
        wrappedFile = VfsFile(jsRuntime.localStorage().vfs, parent.pathInfo.combine(child.pathInfo).fullPath)
    }

    actual constructor(parent: File, child: String) {
        wrappedFile = VfsFile(jsRuntime.localStorage().vfs, parent.getAbsolutePath().pathInfo.combine(child.pathInfo).fullPath)
    }

    constructor(parent: VfsFile, child: String) : this(parent.absolutePath, child)

    constructor(file: VfsFile) {
        wrappedFile = file
    }

    override fun getName(): String = wrappedFile.baseName
    override suspend fun getParent(): String = wrappedFile.parent.absolutePath
    override suspend fun getParentFile(): IPlatformFile = File(getParent())
    override fun getPath(): String = wrappedFile.path
    override suspend fun isAbsolute(): Boolean = true
    override fun getAbsolutePath(): String = wrappedFile.absolutePath
    override fun getAbsoluteFile(): IPlatformFile = File(getAbsolutePath())
    override suspend fun getCanonicalPath(): String = getAbsolutePath()
    override suspend fun getCanonicalFile(): IPlatformFile = getAbsoluteFile()
    override suspend fun getCanRead(): Boolean = true
    override suspend fun getCanWrite(): Boolean = true
    override suspend fun getExists(): Boolean = wrappedFile.exists()
    override suspend fun isDirectory(): Boolean = wrappedFile.isDirectory()
    override suspend fun isFile(): Boolean = wrappedFile.isFile()
    override suspend fun isHidden(): Boolean = false
    override suspend fun getLastModified(): Long = wrappedFile.stat().modifiedTime.unixMillisLong
    override suspend fun getLength(): Long = wrappedFile.size()
    override suspend fun getTotalSpace(): Long = throw IllegalStateException("Not implemented!")
    override suspend fun getFreeSpace(): Long = throw IllegalStateException("Not implemented!")
    override suspend fun getUsableSpace(): Long = throw IllegalStateException("Not implemented!")

    override suspend fun createNewFile(): Boolean {
        throw IllegalStateException("Not implemented!")
    }

    override suspend fun delete(): Boolean {
        return wrappedFile.delete()
    }

    override suspend fun deleteOnExit() {
        throw IllegalStateException("Not implemented!")
    }

    override suspend fun list(): Array<String>? {
        return wrappedFile.listSimple().map { it.absolutePath }.toTypedArray()
    }

    override suspend fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>? {
        return wrappedFile.listRecursiveSimple { filter(File(it.parent.absolutePath), it.baseName) }
            .map { it.absolutePath }.toTypedArray()
    }

    override suspend fun listFiles(): Array<IPlatformFile>? {
        return list()?.map { File(it) }?.toTypedArray()
    }

    override suspend fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>? {
        return list(filter)?.map { File(it) }?.toTypedArray()
    }

    override suspend fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>? {
        return wrappedFile.listRecursiveSimple { filter(File(it.absolutePath)) }
            .map { File(it.absolutePath) }.toTypedArray()
    }

    override suspend fun mkdir(): Boolean {
        return wrappedFile.mkdir()
    }

    override suspend fun mkdirs(): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override suspend fun renameTo(dest: File): Boolean {
        return wrappedFile.renameTo(dest.getAbsolutePath())
    }

    override suspend fun setLastModified(time: Long): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override suspend fun setReadOnly(): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override suspend fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override suspend fun setWritable(writable: Boolean): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override suspend fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override suspend fun setReadable(readable: Boolean): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override suspend fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override suspend fun setExecutable(executable: Boolean): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override suspend fun canExecute(): Boolean {
        throw IllegalArgumentException("Not implemented!")
    }

    override suspend fun openOutputStream(append: Boolean): AsyncOutputStream {
        return wrappedFile.open(if (append && getExists()) VfsOpenMode.APPEND else VfsOpenMode.CREATE)
    }

    override suspend fun openInputStream(): AsyncInputStream {
        return wrappedFile.open(VfsOpenMode.READ)
    }

    override fun hashCode(): Int {
        return wrappedFile.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is IPlatformFile && other.getAbsolutePath() == getAbsolutePath()
    }

    override fun compareTo(other: IPlatformFile): Int {
        return getName().compareTo(other.getName())
    }
}
