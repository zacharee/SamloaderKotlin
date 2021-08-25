package tk.zwander.common.util

import com.soywiz.kmem.Int8Buffer
import com.soywiz.korio.stream.AsyncOutputStream
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.BufferDataSource
import org.khronos.webgl.Int32Array

val Int32Array.outputStream: AsyncOutputStream
    get() = object : AsyncOutputStream {
        val currentOffset: AtomicInt = atomic(0)

        override suspend fun close() {}

        override suspend fun write(buffer: ByteArray, offset: Int, len: Int) {
            println("length $length offset ${currentOffset.value}")

            set(buffer.slice(offset until offset + len).map { it.toInt() }.toTypedArray(), currentOffset.value)

            currentOffset += len
        }
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
