package tk.zwander.common.util

import com.soywiz.korio.lang.unsupported
import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncLengthStream
import com.soywiz.korio.stream.AsyncOutputStream
import java.io.InputStream
import java.io.OutputStream

fun OutputStream.flushingAsync(length: Long? = null): AsyncOutputStream {
    val syncOS = this

    if (length != null) {
        return object : AsyncOutputStream, AsyncLengthStream {
            override suspend fun write(buffer: ByteArray, offset: Int, len: Int) {
                syncOS.write(buffer, offset, len)
            }

            override suspend fun write(byte: Int) {
                syncOS.write(byte)
            }

            override suspend fun close() {
                syncOS.flush()
                syncOS.close()
            }

            override suspend fun setLength(value: Long) {
                unsupported("Can't set length")
            }

            override suspend fun getLength(): Long {
                return length
            }
        }
    } else {
        return object : AsyncOutputStream {
            override suspend fun write(buffer: ByteArray, offset: Int, len: Int) {
                syncOS.write(buffer, offset, len)
            }

            override suspend fun write(byte: Int) {
                syncOS.write(byte)
            }

            override suspend fun close() {
                syncOS.flush()
                syncOS.close()
            }
        }
    }
}

fun InputStream.inputAsync(length: Long? = null): AsyncInputStream {
    val syncIS = this
    if (length != null) {
        return object : AsyncInputStream, AsyncLengthStream {
            override suspend fun read(buffer: ByteArray, offset: Int, len: Int): Int = syncIS.read(buffer, offset, len)
            override suspend fun read(): Int = syncIS.read()
            override suspend fun close() = syncIS.close()
            override suspend fun setLength(value: Long) {
                unsupported("Can't set length")
            }

            override suspend fun getLength(): Long = length
        }
    } else {
        return object : AsyncInputStream {
            override suspend fun read(buffer: ByteArray, offset: Int, len: Int): Int = syncIS.read(buffer, offset, len)
            override suspend fun read(): Int = syncIS.read()
            override suspend fun close() = run { syncIS.close() }
        }
    }
}