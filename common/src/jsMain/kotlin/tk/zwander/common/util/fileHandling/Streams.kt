package tk.zwander.common.util.fileHandling

import com.soywiz.klock.DateTime
import korlibs.io.file.*
import korlibs.io.stream.AsyncOutputStream
import korlibs.io.stream.AsyncStream
import korlibs.io.stream.toAsyncStream
import kotlinx.coroutines.await

suspend fun FileSystemFileHandle.toVfsFile(): VfsFile {
    val file = this.getFile().await()

    return object : Vfs() {
        override val absolutePath: String
            get() = file.name

        override suspend fun open(path: String, mode: VfsOpenMode): AsyncStream {
            return if (mode != VfsOpenMode.READ) {
                this@toVfsFile.createWritable(
                    object : CreateWritableOptions {
                        override val keepExistingData: Boolean
                            get() = mode == VfsOpenMode.APPEND
                    }
                ).await().toAsync().toAsyncStream()
            } else {
                file.openAsync()
            }
        }

        override suspend fun stat(path: String): VfsStat {
            return VfsStat(
                file = file(path),
                exists = true,
                isDirectory = false,
                size = file.size.toLong(),
                modifiedTime = DateTime.Companion.fromUnix(file.lastModified.toLong()),
                kind = FileKind.BINARY
            )
        }
    }[file.name]
}

suspend fun WritableStream.toAsync(): AsyncOutputStream {
    val writer = getWriter()
    val stream = object : AsyncOutputStream {
        override suspend fun close() {
            writer.close().await()
            writer.closed.await()
        }

        override suspend fun write(buffer: ByteArray, offset: Int, len: Int) {
            writer.ready.await()
            writer.write(buffer.sliceArray(offset until offset + len)).await()
        }
    }

    return try {
        stream
    } catch (e: Throwable) {
        stream.close()

        throw e
    }
}
