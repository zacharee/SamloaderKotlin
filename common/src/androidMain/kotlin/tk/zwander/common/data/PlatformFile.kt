package tk.zwander.common.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import korlibs.io.stream.AsyncInputStream
import korlibs.io.stream.AsyncOutputStream
import tk.zwander.common.util.flushingAsync
import tk.zwander.common.util.inputAsync

class PlatformUriFile(
    private val context: Context,
    private val wrappedFile: DocumentFile,
) : IPlatformFile {
    @Suppress("unused")
    constructor(context: Context, file: IPlatformFile) : this(context, (file as PlatformUriFile).wrappedFile)

    @Suppress("unused")
    constructor(context: Context, uri: Uri, isTree: Boolean) : this(
        context,
        if (isTree) {
            DocumentFile.fromTreeUri(context, uri)!!
        } else {
            DocumentFile.fromSingleUri(context, uri)!!
        },
    )

    override fun getName(): String = wrappedFile.name!!
    override suspend fun getParent(): String = wrappedFile.parentFile!!.uri.toString()
    override suspend fun getParentFile(): IPlatformFile = PlatformUriFile(context, wrappedFile.parentFile!!)
    override fun getPath(): String = wrappedFile.uri.toString()
    override suspend fun isAbsolute(): Boolean = false
    override fun getAbsolutePath(): String = getPath()
    override fun getAbsoluteFile(): IPlatformFile = this
    override suspend fun getCanonicalPath(): String = throw IllegalAccessException("Not Supported")
    override suspend fun getCanonicalFile(): File = throw IllegalAccessException("Not Supported")
    override suspend fun getCanRead(): Boolean = wrappedFile.canRead()
    override suspend fun getCanWrite(): Boolean = wrappedFile.canWrite()
    override suspend fun getExists(): Boolean = wrappedFile.exists()
    override suspend fun isDirectory(): Boolean = wrappedFile.isDirectory
    override suspend fun isFile(): Boolean = wrappedFile.isFile
    override suspend fun isHidden(): Boolean = false
    override suspend fun getLastModified(): Long = wrappedFile.lastModified()
    override suspend fun getLength(): Long = wrappedFile.length()
    override suspend fun getTotalSpace(): Long = throw IllegalAccessException("Not Supported")
    override suspend fun getFreeSpace(): Long = throw IllegalAccessException("Not Supported")
    override suspend fun getUsableSpace(): Long = throw IllegalAccessException("Not Supported")

    override suspend fun createNewFile(): Boolean {
        //DocumentFile creates itself.
        return true
    }

    override suspend fun delete(): Boolean {
        return wrappedFile.delete()
    }

    override suspend fun deleteOnExit() {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun list(): Array<String> {
        return wrappedFile.listFiles().map { it.name!! }.toTypedArray()
    }

    override suspend fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String> {
        return wrappedFile.listFiles().filter { filter(PlatformUriFile(context, it.parentFile!!), it.name!!) }
            .map { it.name!! }
            .toTypedArray()
    }

    override suspend fun listFiles(): Array<IPlatformFile> {
        return wrappedFile.listFiles().map { PlatformUriFile(context, it) }
            .toTypedArray()
    }

    override suspend fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile> {
        return wrappedFile.listFiles().filter { filter(PlatformUriFile(context, it.parentFile!!), it.name!!) }
            .map { PlatformUriFile(context, it) }
            .toTypedArray()
    }

    override suspend fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile> {
        return wrappedFile.listFiles().filter { filter(PlatformUriFile(context, it)) }
            .map { PlatformUriFile(context, it) }
            .toTypedArray()
    }

    override suspend fun mkdir(): Boolean {
        return true
    }

    override suspend fun mkdirs(): Boolean {
        return true
    }

    override suspend fun renameTo(dest: File): Boolean {
        return wrappedFile.renameTo(dest.getName())
    }

    override suspend fun setLastModified(time: Long): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setReadOnly(): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setWritable(writable: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setReadable(readable: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setExecutable(executable: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun canExecute(): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun openOutputStream(append: Boolean): AsyncOutputStream {
        return context.contentResolver.openOutputStream(wrappedFile.uri, "w${if (append) "a" else ""}")!!.flushingAsync()
    }

    override suspend fun openInputStream(): AsyncInputStream {
        return context.contentResolver.openInputStream(wrappedFile.uri)!!.inputAsync()
    }

    override fun compareTo(other: IPlatformFile): Int {
        if (other !is PlatformUriFile) return -1

        return wrappedFile.uri.compareTo(other.wrappedFile.uri)
    }

    override fun equals(other: Any?): Boolean {
        return other is PlatformUriFile
                && wrappedFile.uri == other.wrappedFile.uri
    }

    override fun hashCode(): Int {
        return wrappedFile.uri.hashCode()
    }
}
