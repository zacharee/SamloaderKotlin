package tk.zwander.common.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream
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

    override val name: String
        get() = this.wrappedFile.name
    override val parent: String
        get() = this.wrappedFile.parent
    override val parentFile: File
        get() = File(this.wrappedFile.parentFile.absolutePath)
    override val path: String
        get() = this.wrappedFile.path
    override val isAbsolute: Boolean
        get() = this.wrappedFile.isAbsolute
    override val absolutePath: String
        get() = this.wrappedFile.absolutePath
    override val absoluteFile: File
        get() = File(this.wrappedFile.absoluteFile.absolutePath)
    override val canonicalPath: String
        get() = this.wrappedFile.canonicalPath
    override val canonicalFile: File
        get() = File(this.wrappedFile.canonicalFile.absolutePath)
    override val canRead: Boolean
        get() = this.wrappedFile.canRead
    override val canWrite: Boolean
        get() = this.wrappedFile.canWrite
    override val exists: Boolean
        get() = this.wrappedFile.exists
    override val isDirectory: Boolean
        get() = this.wrappedFile.isDirectory
    override val isFile: Boolean
        get() = this.wrappedFile.isFile
    override val isHidden: Boolean
        get() = this.wrappedFile.isHidden
    override val lastModified: Long
        get() = this.wrappedFile.lastModified
    override val length: Long
        get() = this.wrappedFile.length
    override val totalSpace: Long
        get() = this.wrappedFile.totalSpace
    override val freeSpace: Long
        get() = this.wrappedFile.freeSpace
    override val usableSpace: Long
        get() = this.wrappedFile.usableSpace

    override fun createNewFile(): Boolean {
        return this.wrappedFile.createNewFile()
    }

    override fun delete(): Boolean {
        return this.wrappedFile.delete()
    }

    override fun deleteOnExit() {
        this.wrappedFile.deleteOnExit()
    }

    override fun list(): Array<String>? {
        return this.wrappedFile.list()
    }

    override fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>? {
        return this.wrappedFile.list { dir, name -> filter(File(dir.absolutePath), name) }
    }

    override fun listFiles(): Array<IPlatformFile>? {
        return this.wrappedFile.listFiles()?.map { File(it.absolutePath) }
            ?.toTypedArray()
    }

    override fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>? {
        return this.wrappedFile.listFiles { dir, name -> filter(File(dir.absolutePath), name) }
            ?.map { File(it.absolutePath) }
            ?.toTypedArray()
    }

    override fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>? {
        return this.wrappedFile.listFiles { pathname -> filter(File(pathname.absolutePath)) }
            ?.map { File(it.absolutePath) }
            ?.toTypedArray()
    }

    override fun mkdir(): Boolean {
        return this.wrappedFile.mkdir()
    }

    override fun mkdirs(): Boolean {
        return this.wrappedFile.mkdirs()
    }

    override fun renameTo(dest: File): Boolean {
        return this.wrappedFile.renameTo(dest)
    }

    override fun setLastModified(time: Long): Boolean {
        return this.wrappedFile.setLastModified(time)
    }

    override fun setReadOnly(): Boolean {
        return this.wrappedFile.setReadOnly()
    }

    override fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        return this.wrappedFile.setWritable(writable, ownerOnly)
    }

    override fun setWritable(writable: Boolean): Boolean {
        return this.wrappedFile.setWritable(writable)
    }

    override fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        return this.wrappedFile.setReadable(readable, ownerOnly)
    }

    override fun setReadable(readable: Boolean): Boolean {
        return this.wrappedFile.setReadable(readable)
    }

    override fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        return this.wrappedFile.setExecutable(executable, ownerOnly)
    }

    override fun setExecutable(executable: Boolean): Boolean {
        return this.wrappedFile.setExecutable(executable)
    }

    override fun canExecute(): Boolean {
        return this.wrappedFile.canExecute()
    }

    override fun openOutputStream(append: Boolean): AsyncOutputStream {
        return wrappedFile.openOutputStream(append)
    }

    override fun openInputStream(): AsyncInputStream {
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
        wrappedFile = java.io.File(java.io.File(parent.absolutePath), child)
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
        return FileInputStream(wrappedFile).inputAsync()
    }

    override fun compareTo(other: IPlatformFile): Int {
        return wrappedFile.compareTo(java.io.File(other.absolutePath))
    }

    override fun hashCode(): Int {
        return wrappedFile.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is PlatformFileFile
                && other.absolutePath == absolutePath
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

    override val name: String
        get() = wrappedFile.name!!
    override val parent: String
        get() = wrappedFile.parentFile!!.uri.toString()
    override val parentFile: IPlatformFile
        get() = PlatformUriFile(context, wrappedFile.parentFile!!)
    override val path: String
        get() = wrappedFile.uri.toString()
    override val isAbsolute: Boolean
        get() = false
    override val absolutePath: String
        get() = path
    override val absoluteFile: IPlatformFile
        get() = this
    override val canonicalPath: String
        get() = throw IllegalAccessException("Not Supported")
    override val canonicalFile: File
        get() = throw IllegalAccessException("Not Supported")
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
        get() = false
    override val lastModified: Long
        get() = wrappedFile.lastModified()
    override val length: Long
        get() = wrappedFile.length()
    override val totalSpace: Long
        get() = throw IllegalAccessException("Not Supported")
    override val freeSpace: Long
        get() = throw IllegalAccessException("Not Supported")
    override val usableSpace: Long
        get() = throw IllegalAccessException("Not Supported")

    override fun createNewFile(): Boolean {
        //DocumentFile creates itself.
        return true
    }

    override fun delete(): Boolean {
        return wrappedFile.delete()
    }

    override fun deleteOnExit() {
        throw IllegalAccessException("Not Supported")
    }

    override fun list(): Array<String> {
        return wrappedFile.listFiles().map { it.name!! }.toTypedArray()
    }

    override fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String> {
        return wrappedFile.listFiles().filter { filter(PlatformUriFile(context, it.parentFile!!), it.name!!) }
            .map { it.name!! }
            .toTypedArray()
    }

    override fun listFiles(): Array<IPlatformFile> {
        return wrappedFile.listFiles().map { PlatformUriFile(context, it) }
            .toTypedArray()
    }

    override fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile> {
        return wrappedFile.listFiles().filter { filter(PlatformUriFile(context, it.parentFile!!), it.name!!) }
            .map { PlatformUriFile(context, it) }
            .toTypedArray()
    }

    override fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile> {
        return wrappedFile.listFiles().filter { filter(PlatformUriFile(context, it)) }
            .map { PlatformUriFile(context, it) }
            .toTypedArray()
    }

    override fun mkdir(): Boolean {
        return true
    }

    override fun mkdirs(): Boolean {
        return true
    }

    override fun renameTo(dest: File): Boolean {
        return wrappedFile.renameTo(dest.name)
    }

    override fun setLastModified(time: Long): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override fun setReadOnly(): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override fun setWritable(writable: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override fun setReadable(readable: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override fun setExecutable(executable: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override fun canExecute(): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override fun openOutputStream(append: Boolean): AsyncOutputStream {
        return context.contentResolver.openOutputStream(wrappedFile.uri, "w${if (append) "a" else ""}").flushingAsync()
    }

    override fun openInputStream(): AsyncInputStream {
        return context.contentResolver.openInputStream(wrappedFile.uri).inputAsync()
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