package tk.zwander.common.util

import com.soywiz.kmem.Int8Buffer
import com.soywiz.korio.stream.AsyncOutputStream
import io.ktor.client.fetch.*
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

fun newArrayBuffer(size: dynamic): ArrayBuffer {
    return js("new ArrayBuffer(size);") as ArrayBuffer
}

val Int8Buffer.outputStream: AsyncOutputStream
    get() = object : AsyncOutputStream {
        val currentOffset: AtomicInt = atomic(0)

        override suspend fun close() {}

        override suspend fun write(buffer: ByteArray, offset: Int, len: Int) {
            set(buffer.sliceArray(offset until offset + len).toTypedArray().apply { println("len $len slice size $size") }, currentOffset.value)

            currentOffset += len
        }
    }

@Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun createWriteStream(fileName: String, fileSize: Long): WritableStream<Uint8Array> {
    return js("""
        var streamSaver = require('streamsaver');
        
        streamSaver.createWriteStream(fileName, { size: fileSize });
    """) as WritableStream<Uint8Array>
}

fun <W> WritableStreamDefaultWriter<W>.openAsync(transform: (chunk: ByteArray) -> W): AsyncOutputStream {
    return object : AsyncOutputStream {
        override suspend fun close() {
            this@openAsync.close().await()
        }

        override suspend fun write(buffer: ByteArray, offset: Int, len: Int) {
            this@openAsync.write(transform(buffer.sliceArray(offset until offset + len)))
        }
    }
}
