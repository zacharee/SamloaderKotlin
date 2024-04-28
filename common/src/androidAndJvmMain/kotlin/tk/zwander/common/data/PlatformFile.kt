@file:JvmName("AndroidJVMCommonPlatformFile")

package tk.zwander.common.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.sink
import okio.source

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

    @Suppress("unused")
    constructor(parent: java.io.File, child: String) {
        wrappedFile = java.io.File(parent, child)
    }

    constructor(file: java.io.File) {
        wrappedFile = file
    }

    actual override fun getName(): String = wrappedFile.name
    actual override suspend fun getParent(): String? = wrappedFile.parent
    actual override suspend fun getParentFile(): IPlatformFile? =
        wrappedFile.parentFile?.absolutePath?.let { File(it) }

    actual override fun getPath(): String = wrappedFile.path
    actual override suspend fun isAbsolute(): Boolean = wrappedFile.isAbsolute
    actual override fun getAbsolutePath(): String = wrappedFile.absolutePath
    actual override fun getAbsoluteFile(): IPlatformFile =
        File(wrappedFile.absoluteFile.absolutePath)

    actual override suspend fun getCanonicalPath(): String = wrappedFile.canonicalPath
    actual override suspend fun getCanonicalFile(): IPlatformFile =
        File(wrappedFile.canonicalFile.absolutePath)

    actual override suspend fun getCanRead(): Boolean = wrappedFile.canRead()
    actual override suspend fun getCanWrite(): Boolean = wrappedFile.canWrite()
    actual override suspend fun getExists(): Boolean = wrappedFile.exists()
    actual override suspend fun isDirectory(): Boolean = wrappedFile.isDirectory
    actual override suspend fun isFile(): Boolean = wrappedFile.isFile
    actual override suspend fun isHidden(): Boolean = wrappedFile.isHidden
    actual override suspend fun getLastModified(): Long = wrappedFile.lastModified()
    actual override suspend fun getLength(): Long = wrappedFile.length()
    actual override suspend fun getTotalSpace(): Long = wrappedFile.totalSpace
    actual override suspend fun getFreeSpace(): Long = wrappedFile.freeSpace
    actual override suspend fun getUsableSpace(): Long = wrappedFile.usableSpace

    actual override suspend fun createNewFile(): Boolean {
        return withContext(Dispatchers.IO) {
            wrappedFile.createNewFile()
        }
    }

    actual override suspend fun delete(): Boolean {
        return wrappedFile.delete()
    }

    actual override suspend fun deleteOnExit() {
        wrappedFile.deleteOnExit()
    }

    actual override suspend fun list(): Array<String>? {
        return wrappedFile.list()
    }

    actual override suspend fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>? {
        return wrappedFile.list { dir, name -> filter(File(dir.absolutePath), name) }
    }

    actual override suspend fun listFiles(): Array<IPlatformFile>? {
        return wrappedFile.listFiles()?.map { File(it.absolutePath) }
            ?.toTypedArray()
    }

    actual override suspend fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>? {
        return wrappedFile.listFiles { dir, name -> filter(File(dir.absolutePath), name) }
            ?.map { File(it.absolutePath) }
            ?.toTypedArray()
    }

    actual override suspend fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>? {
        return wrappedFile.listFiles { pathname -> filter(File(pathname.absolutePath)) }
            ?.map { File(it.absolutePath) }
            ?.toTypedArray()
    }

    actual override suspend fun mkdir(): Boolean {
        return wrappedFile.mkdir()
    }

    actual override suspend fun mkdirs(): Boolean {
        return wrappedFile.mkdirs()
    }

    actual override suspend fun renameTo(dest: File): Boolean {
        return wrappedFile.renameTo(java.io.File(dest.getAbsolutePath()))
    }

    actual override suspend fun setLastModified(time: Long): Boolean {
        return wrappedFile.setLastModified(time)
    }

    actual override suspend fun setReadOnly(): Boolean {
        return wrappedFile.setReadOnly()
    }

    actual override suspend fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        return wrappedFile.setWritable(writable, ownerOnly)
    }

    actual override suspend fun setWritable(writable: Boolean): Boolean {
        return wrappedFile.setWritable(writable)
    }

    actual override suspend fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        return wrappedFile.setReadable(readable, ownerOnly)
    }

    actual override suspend fun setReadable(readable: Boolean): Boolean {
        return wrappedFile.setReadable(readable)
    }

    actual override suspend fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        return wrappedFile.setExecutable(executable, ownerOnly)
    }

    actual override suspend fun setExecutable(executable: Boolean): Boolean {
        return wrappedFile.setExecutable(executable)
    }

    actual override suspend fun canExecute(): Boolean {
        return wrappedFile.canExecute()
    }

    actual override suspend fun openOutputStream(append: Boolean): BufferedSink? {
        return withContext(Dispatchers.IO) {
            wrappedFile.sink(append).buffer()
        }
    }

    actual override suspend fun openInputStream(): BufferedSource? {
        return withContext(Dispatchers.IO) {
            wrappedFile.source().buffer()
        }
    }

    actual override fun hashCode(): Int {
        return wrappedFile.hashCode()
    }

    actual override fun compareTo(other: IPlatformFile): Int {
        return wrappedFile.compareTo(java.io.File(other.getAbsolutePath()))
    }

    actual override fun equals(other: Any?): Boolean {
        return other is PlatformFile && wrappedFile.absolutePath == other.getAbsolutePath()
    }
}
