package tk.zwander.common.util

import korlibs.io.stream.AsyncInputStream
import korlibs.io.stream.AsyncLengthStream
import korlibs.io.stream.AsyncOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

fun OutputStream.flushingAsync(length: Long? = null): AsyncOutputStream {
    val syncOS = this

    if (length != null) {
        return object : AsyncOutputStream, AsyncLengthStream {
            override suspend fun write(buffer: ByteArray, offset: Int, len: Int) {
                withContext(Dispatchers.IO) {
                    syncOS.write(buffer, offset, len)
                }
            }

            override suspend fun write(byte: Int) {
                withContext(Dispatchers.IO) {
                    syncOS.write(byte)
                }
            }

            override suspend fun close() {
                withContext(Dispatchers.IO) {
                    syncOS.flush()
                    syncOS.close()
                }
            }

            override suspend fun setLength(value: Long) {
                error("Can't set length")
            }

            override suspend fun getLength(): Long {
                return length
            }
        }
    } else {
        return object : AsyncOutputStream {
            override suspend fun write(buffer: ByteArray, offset: Int, len: Int) {
                withContext(Dispatchers.IO) {
                    syncOS.write(buffer, offset, len)
                }
            }

            override suspend fun write(byte: Int) {
                withContext(Dispatchers.IO) {
                    syncOS.write(byte)
                }
            }

            override suspend fun close() {
                withContext(Dispatchers.IO) {
                    syncOS.flush()
                    syncOS.close()
                }
            }
        }
    }
}

fun InputStream.inputAsync(length: Long? = null): AsyncInputStream {
    val syncIS = this
    if (length != null) {
        return object : AsyncInputStream, AsyncLengthStream {
            override suspend fun read(buffer: ByteArray, offset: Int, len: Int): Int =
                withContext(Dispatchers.IO) {
                    syncIS.read(buffer, offset, len)
                }
            override suspend fun read(): Int =
                withContext(Dispatchers.IO) {
                    syncIS.read()
                }
            override suspend fun close() =
                withContext(Dispatchers.IO) {
                    syncIS.close()
                }
            override suspend fun setLength(value: Long) {
                error("Can't set length")
            }

            override suspend fun getLength(): Long = length
        }
    } else {
        return object : AsyncInputStream {
            override suspend fun read(buffer: ByteArray, offset: Int, len: Int): Int =
                withContext(Dispatchers.IO) {
                    syncIS.read(buffer, offset, len)
                }
            override suspend fun read(): Int =
                withContext(Dispatchers.IO) {
                    syncIS.read()
                }
            override suspend fun close() =
                withContext(Dispatchers.IO) {
                    syncIS.close()
                }
        }
    }
}
