package tk.zwander.common.util

import korlibs.io.stream.AsyncInputStream
import korlibs.io.stream.AsyncOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.time.measureTime

suspend fun streamOperationWithProgress(
    input: AsyncInputStream,
    output: AsyncOutputStream,
    size: Long,
    progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit,
    operation: (suspend CoroutineScope.(buffer: ByteArray) -> ByteArray)? = null,
    chunkSize: Int = 0x300000,
    progressOffset: Long = 0L,
) {
    withContext(Dispatchers.IO) {
        val buffer = ByteArray(chunkSize)

        var len: Int
        var totalLen = 0L

        val averager = Averager()

        while (isActive) {
            val nano = measureTime {
                len = input.read(buffer, 0, buffer.size)
                totalLen += len

                if (len > 0) {
                    val exactData = if (len == buffer.size) {
                        buffer
                    } else {
                        buffer.sliceArray(0 until len)
                    }

                    val result = operation?.invoke(this, exactData) ?: exactData

                    output.write(result, 0, result.size)
                }
            }.inWholeNanoseconds

            if (len <= 0) break

            val lenF = len
            val totalLenF = totalLen

            async {
                averager.update(nano, lenF.toLong())
                val (totalTime, totalRead, _) = averager.sum()

                progressCallback(
                    totalLenF + progressOffset,
                    size,
                    (totalRead / (totalTime.toDouble() / 1_000_000_000.0)).toLong(),
                )
            }
        }

        input.close()
        output.close()
    }
}
