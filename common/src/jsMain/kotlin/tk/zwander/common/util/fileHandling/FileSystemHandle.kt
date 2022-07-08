package tk.zwander.common.util.fileHandling

import io.ktor.client.fetch.*
import org.w3c.files.File
import kotlin.js.Promise

external interface FileSystemHandlePermissionDescriptor {
    val mode: String
}

external interface PermissionStatus {
    val state: String
}

external interface GetHandleOptions {
    val create: Boolean
}

external interface RemoveEntryOptions {
    val recursive: Boolean
}

external interface CreateWritableOptions {
    val keepExistingData: Boolean
}

external interface WriteOptions {
    val type: String
    val data: ArrayBuffer?
    val position: Long?
    val size: Long?
}

external interface FileSystemHandle {
    val kind: String
    val name: String

    fun isSameEntry(other: FileSystemHandle): Promise<Boolean>
    fun queryPermission(descriptor: FileSystemHandlePermissionDescriptor): Promise<PermissionStatus>
    fun requestPermission(descriptor: FileSystemHandlePermissionDescriptor): Promise<PermissionStatus>
}

external interface FileSystemDirectoryHandle : FileSystemHandle {
    fun entries(): Array<Pair<String, FileSystemHandle>>
    fun getFileHandle(name: String, options: GetHandleOptions? = definedExternally) : Promise<FileSystemFileHandle>
    fun getDirectoryHandle(name: String, options: GetHandleOptions? = definedExternally) : Promise<FileSystemDirectoryHandle>
    fun keys(): Array<String>
    fun values(): Array<FileSystemHandle>
    fun resolve(name: String): Promise<Array<String>>?
    fun removeEntry(name: String, options: RemoveEntryOptions? = definedExternally): Promise<Unit>

}

external interface FileSystemFileHandle : FileSystemHandle {
    fun getFile(): Promise<File>
    fun createWritable(options: CreateWritableOptions): Promise<FileSystemWritableFileStream>
}

external interface WritableStream {
    val locked: Boolean

    fun abort(reason: String? = definedExternally): Promise<String?>
    fun close(): Promise<Unit>
    fun getWriter(): WritableStreamDefaultWriter
}

external interface WritableStreamDefaultWriter {
    val closed: Boolean
    val desiredSize: Long
    val ready: Promise<Unit>

    fun abort(reason: String? = definedExternally): Promise<String?>
    fun close(): Promise<Unit>
    fun releaseLock()
    fun write(chunk: ByteArray): Promise<Unit>
}

external interface FileSystemWritableFileStream : WritableStream {
    fun write(data: ArrayBuffer): Promise<Unit>
    fun write(data: WriteOptions): Promise<Unit>
    fun seek(position: Long): Promise<Unit>
    fun truncate(size: Long? = definedExternally): Promise<Unit>
}
