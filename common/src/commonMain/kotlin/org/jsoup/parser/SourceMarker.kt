package org.jsoup.parser

import com.soywiz.korio.stream.SyncInputStream
import com.soywiz.korio.stream.readBytes
import okio.*
import kotlin.math.max
import kotlin.math.min

/**
 * Builds a buffered source that can rewind to a marked position earlier in the stream.
 *
 *
 * Mark potential positions to rewind back to with [.mark]; rewind back to these positions
 * with [.reset]. Both operations apply to the position in the [buffered][.source]; resetting will impact the buffer.
 *
 *
 * When marking it is necessary to specify how much data to retain. Once you advance above this
 * limit, the mark is discarded and resetting is not permitted. This may be used to lookahead a
 * fixed number of bytes without loading an entire stream into memory. To reset an arbitrary
 * number of bytes use `mark(Long#MAX_VALUE)`.
 */
open class SourceMarker(source: Source) {
    /*
   * This class wraps the underlying source in a MarkSource to support mark and reset. It creates a
   * BufferedSource for the caller so that it can track its offsets and manipulate its buffer.
   */
    /**
     * The offset into the underlying source. To compute the user's offset start with this and
     * subtract userBuffer.size.
     */
    var offset: Long = 0

    /** The offset of the earliest mark, or -1 for no mark.  */
    var mark = -1L

    /** The offset of the latest readLimit, or -1 for no mark.  */
    var limit = -1L
    var closed = false
    val markSource: MarkSource
    val userSource: BufferedSource

    /** A copy of the underlying source's data beginning at `mark`.  */
    val markBuffer: Buffer

    /** Just the userSource's buffer.  */
    val userBuffer: Buffer

    init {
        markSource = MarkSource(source)
        markBuffer = Buffer()
        userSource = markSource.buffer()
        userBuffer = userSource.buffer
    }

    fun source(): BufferedSource {
        return userSource
    }

    /**
     * Marks the current position in the stream as one to potentially return back to. Returns the
     * offset of this position. Call [.reset] with this position to return to it later. It
     * is an error to call [.reset] after consuming more than `readLimit` bytes from
     * [the source][.source].
     */
    @Throws(IOException::class)
    open fun mark(readLimit: Long): Long {
        if (readLimit < 0L) {
            throw IllegalArgumentException("readLimit < 0: $readLimit")
        }
        if (closed) {
            throw IllegalStateException("closed")
        }

        // Mark the current position in the buffered source.
        val userOffset: Long = offset - userBuffer.size

        // If this is a new mark promote userBuffer data into the markBuffer.
        if (mark == -1L) {
            markBuffer.writeAll(userBuffer)
            mark = userOffset
            offset = userOffset
        }

        // Grow the limit if necessary.
        var newMarkBufferLimit = userOffset + readLimit
        if (newMarkBufferLimit < 0) newMarkBufferLimit = Long.MAX_VALUE // Long overflow!
        limit = max(limit, newMarkBufferLimit)
        return userOffset
    }

    /** Resets [the source][.source] to `userOffset`.  */
    @Throws(IOException::class)
    open fun reset(userOffset: Long) {
        if (closed) {
            throw IllegalStateException("closed")
        }
        if (userOffset < mark || userOffset > limit || userOffset > mark + markBuffer.size || offset - userBuffer.size > limit) { // Stream advanced beyond limit.
            throw IOException("cannot reset to $userOffset: out of range")
        }

        // Clear userBuffer to cause data at 'offset' to be returned by the next read.
        offset = userOffset
        userBuffer.clear()
    }

    fun rewind() {
        mark = -1
        offset = 0
        userBuffer.clear()
    }

    inner class MarkSource(source: Source) : ForwardingSource(source) {
        @Throws(IOException::class)
        override fun read(sink: Buffer, byteCount: Long): Long {
            if (closed) {
                throw IllegalStateException("closed")
            }

            // If there's no mark, go to the underlying source.
            if (mark == -1L) {
                val result: Long = super.read(sink, byteCount)
                if (result == -1L) return -1L
                offset += result
                return result
            }

            // If we can read from markBuffer, do that.
            if (offset < mark + markBuffer.size) {
                val posInBuffer = offset - mark
                val result: Long = min(byteCount, markBuffer.size - posInBuffer)
                markBuffer.copyTo(sink, posInBuffer, result)
                offset += result
                return result
            }

            // If we can write to markBuffer, do that.
            if (offset < limit) {
                val byteCountBeforeLimit: Long = limit - (mark + markBuffer.size)
                val result: Long = super.read(markBuffer, min(byteCount, byteCountBeforeLimit))
                if (result == -1L) return -1L
                markBuffer.copyTo(sink, markBuffer.size - result, result)
                offset += result
                return result
            }

            // Attempt to read past the limit. Data will not be saved.
            val result: Long = super.read(sink, byteCount)
            if (result == -1L) return -1L

            // We read past the limit. Discard marked data.
            markBuffer.clear()
            mark = -1L
            limit = -1L
            return result
        }

        @Throws(IOException::class)
        override fun close() {
            if (closed) return
            closed = true
            markBuffer.clear()
            super.close()
        }
    }
}

fun SyncInputStream.source(): Source {
    return object : Source {
        override fun close() {
            this@source.close()
        }

        override fun timeout(): Timeout {
            return Timeout.NONE
        }

        override fun read(sink: Buffer, byteCount: Long): Long {
            val result = this@source.readBytes(byteCount.toInt())
            sink.write(result)
            return result.size.toLong()
        }
    }
}
