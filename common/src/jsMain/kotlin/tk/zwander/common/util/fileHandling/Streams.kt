package tk.zwander.common.util.fileHandling

import com.soywiz.korio.file.*
import com.soywiz.korio.stream.AsyncOutputStream
import com.soywiz.korio.stream.AsyncStream
import com.soywiz.korio.stream.toAsyncStream
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
    }[file.name]
}

fun WritableStream.toAsync(): AsyncOutputStream {
    val writer = getWriter()
    val stream = object : AsyncOutputStream {
        override suspend fun close() {
            if (!locked) {
                writer.close()
            }
        }

        override suspend fun write(buffer: ByteArray, offset: Int, len: Int) {
            writer.write(buffer.sliceArray(offset until offset + len))
        }
    }

    return try {
        stream
    } catch (e: Throwable) {
        if (!writer.closed) {
            writer.close()
        }

        throw e
    }
}
