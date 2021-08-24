package tk.zwander.common.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream
import kotlinx.coroutines.CoroutineScope
import tk.zwander.common.util.inputAsync
import tk.zwander.common.util.flushingAsync
import java.io.FileInputStream
import java.io.FileOutputStream

actual open class PlatformFile : File {
    private val wrappedFile: IPlatformFile

    actual constructor(pathName: String) {
        this.wrappedFile = PlatformFileFile(pathName)
    }

    actual constructor(parent: String, child: String) {
        this.wrappedFile = PlatformFileFile(parent, child)
    }

    actual constructor(parent: File, child: String) {
        this.wrappedFile = PlatformFileFile(parent, child)
    }

    constructor(context: Context, uri: Uri, isTree: Boolean) {
        this.wrappedFile = PlatformUriFile(context, uri, isTree)
    }

    protected constructor(wrappedFile: IPlatformFile) {
        this.wrappedFile = wrappedFile
    }

    override fun hashCode(): Int {
        return this.wrappedFile.hashCode()
    }

    override fun getName(): String = this.wrappedFile.getName()
    override suspend fun getParent(): String? = this.wrappedFile.getParent()
    override suspend fun getParentFile(): File? = this.wrappedFile.getParentFile()?.getAbsolutePath()?.let { File(it) }
    override fun getPath(): String = this.wrappedFile.getPath()
    override suspend fun isAbsolute(): Boolean = this.wrappedFile.isAbsolute()
    override fun getAbsolutePath(): String = this.wrappedFile.getAbsolutePath()
    override fun getAbsoluteFile(): File = File(this.wrappedFile.getAbsoluteFile().getAbsolutePath())
    override suspend fun getCanonicalPath(): String = this.wrappedFile.getCanonicalPath()
    override suspend fun getCanonicalFile(): File = File(this.wrappedFile.getCanonicalFile().getAbsolutePath())
    override suspend fun getCanRead(): Boolean = this.wrappedFile.getCanRead()
    override suspend fun getCanWrite(): Boolean = this.wrappedFile.getCanWrite()
    override suspend fun getExists(): Boolean = this.wrappedFile.getExists()
    override suspend fun isDirectory(): Boolean = this.wrappedFile.isDirectory()
    override suspend fun isFile(): Boolean = this.wrappedFile.isFile()
    override suspend fun isHidden(): Boolean = this.wrappedFile.isHidden()
    override suspend fun getLastModified(): Long = this.wrappedFile.getLastModified()
    override suspend fun getLength(): Long = this.wrappedFile.getLength()
    override suspend fun getTotalSpace(): Long = this.wrappedFile.getTotalSpace()
    override suspend fun getFreeSpace(): Long = this.wrappedFile.getFreeSpace()
    override suspend fun getUsableSpace(): Long = this.wrappedFile.getUsableSpace()

    override suspend fun createNewFile(): Boolean {
        return this.wrappedFile.createNewFile()
    }

    override suspend fun delete(): Boolean {
        return this.wrappedFile.delete()
    }

    override suspend fun deleteOnExit() {
        this.wrappedFile.deleteOnExit()
    }

    override suspend fun list(): Array<String>? {
        return this.wrappedFile.list()
    }

    override suspend fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>? {
        return this.wrappedFile.list { dir, name -> filter(File(dir.getAbsolutePath()), name) }
    }

    override suspend fun listFiles(): Array<IPlatformFile>? {
        return this.wrappedFile.listFiles()?.map { File(it.getAbsolutePath()) }
            ?.toTypedArray()
    }

    override suspend fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>? {
        return this.wrappedFile.listFiles { dir, name -> filter(File(dir.getAbsolutePath()), name) }
            ?.map { File(it.getAbsolutePath()) }
            ?.toTypedArray()
    }

    override suspend fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>? {
        return this.wrappedFile.listFiles { pathname -> filter(File(pathname.getAbsolutePath())) }
            ?.map { File(it.getAbsolutePath()) }
            ?.toTypedArray()
    }

    override suspend fun mkdir(): Boolean {
        return this.wrappedFile.mkdir()
    }

    override suspend fun mkdirs(): Boolean {
        return this.wrappedFile.mkdirs()
    }

    override suspend fun renameTo(dest: File): Boolean {
        return this.wrappedFile.renameTo(dest)
    }

    override suspend fun setLastModified(time: Long): Boolean {
        return this.wrappedFile.setLastModified(time)
    }

    override suspend fun setReadOnly(): Boolean {
        return this.wrappedFile.setReadOnly()
    }

    override suspend fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        return this.wrappedFile.setWritable(writable, ownerOnly)
    }

    override suspend fun setWritable(writable: Boolean): Boolean {
        return this.wrappedFile.setWritable(writable)
    }

    override suspend fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        return this.wrappedFile.setReadable(readable, ownerOnly)
    }

    override suspend fun setReadable(readable: Boolean): Boolean {
        return this.wrappedFile.setReadable(readable)
    }

    override suspend fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        return this.wrappedFile.setExecutable(executable, ownerOnly)
    }

    override suspend fun setExecutable(executable: Boolean): Boolean {
        return this.wrappedFile.setExecutable(executable)
    }

    override suspend fun canExecute(): Boolean {
        return this.wrappedFile.canExecute()
    }

    override suspend fun openOutputStream(append: Boolean): AsyncOutputStream {
        return wrappedFile.openOutputStream(append)
    }

    override suspend fun openInputStream(): AsyncInputStream {
        return wrappedFile.openInputStream()
    }

    override fun compareTo(other: IPlatformFile): Int {
        return this.wrappedFile.compareTo(other)
    }

    override fun equals(other: Any?): Boolean {
        return wrappedFile == other
    }
}

class PlatformFileFile : PlatformFile {
    private val wrappedFile: java.io.File

    constructor(pathName: String) : super(pathName) {
        wrappedFile = java.io.File(pathName)
    }

    constructor(parent: String, child: String) : super(parent, child) {
        wrappedFile = java.io.File(parent, child)
    }

    constructor(parent: File, child: String) : super(parent, child) {
        wrappedFile = java.io.File(java.io.File(parent.getAbsolutePath()), child)
    }

    override fun getName(): String = wrappedFile.name
    override suspend fun getParent(): String? = wrappedFile.parent
    override suspend fun getParentFile(): File? = wrappedFile.parentFile?.absolutePath?.let { File(it) }
    override fun getPath(): String = wrappedFile.path
    override suspend fun isAbsolute(): Boolean = wrappedFile.isAbsolute
    override fun getAbsolutePath(): String = wrappedFile.absolutePath
    override fun getAbsoluteFile(): File = File(wrappedFile.absoluteFile.absolutePath)
    override suspend fun getCanonicalPath(): String = wrappedFile.canonicalPath
    override suspend fun getCanonicalFile(): File = File(wrappedFile.canonicalFile.absolutePath)
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
        return FileInputStream(wrappedFile).inputAsync()
    }

    override fun compareTo(other: IPlatformFile): Int {
        return wrappedFile.compareTo(java.io.File(other.getAbsolutePath()))
    }

    override fun hashCode(): Int {
        return wrappedFile.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is PlatformFileFile
                && other.getAbsolutePath() == getAbsolutePath()
    }
}

class PlatformUriFile : IPlatformFile {
    private val context: Context
    private val wrappedFile: DocumentFile

    constructor(context: Context, file: DocumentFile) {
        this.context = context
        wrappedFile = file
    }

    constructor(context: Context, file: IPlatformFile) {
        assert(file is PlatformUriFile)

        this.context = context
        wrappedFile = (file as PlatformUriFile).wrappedFile
    }

    constructor(context: Context, uri: Uri, isTree: Boolean) {
        this.context = context
        wrappedFile = if (isTree) DocumentFile.fromTreeUri(context, uri)!! else DocumentFile.fromSingleUri(context, uri)!!
    }

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
