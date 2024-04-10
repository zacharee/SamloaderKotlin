package tk.zwander.common.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.BufferedSource
import kotlin.time.measureTime

suspend fun streamOperationWithProgress(
    input: BufferedSource,
    output: BufferedSink,
    size: Long,
    progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit,
    operation: (suspend CoroutineScope.(buffer: ByteArray) -> ByteArray)? = null,
    chunkSize: Int = 0x300000,
    progressOffset: Long = 0L,
) {
    val buffer = ByteArray(chunkSize)

    trackOperationProgress(
        size = size,
        progressCallback = progressCallback,
        operation = {
            val len = input.read(buffer, 0, buffer.size)

            if (len > 0) {
                val exactData = if (len == buffer.size) {
                    buffer
                } else {
                    buffer.sliceArray(0 until len)
                }

                val result = operation?.invoke(this, exactData) ?: exactData

                output.write(result, 0, result.size)
            }

            len.toLong()
        },
        progressOffset = progressOffset,
    )

    input.close()
    output.close()
}

suspend fun trackOperationProgress(
    size: Long,
    progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit,
    operation: suspend CoroutineScope.() -> Long,
    progressOffset: Long = 0L,
    condition: () -> Boolean = { true },
) {
    withContext(Dispatchers.IO) {
        var len: Long
        var totalLen = 0L

        val averager = Averager()

        while (isActive && condition()) {
            val nano = measureTime {
                len = operation()
                totalLen += len
            }.inWholeNanoseconds

            if (len <= 0) break

            val lenF = len
            val totalLenF = totalLen

            launch {
                averager.update(nano, lenF)
                val (totalTime, totalRead, _) = averager.sum()

                progressCallback(
                    totalLenF + progressOffset,
                    size,
                    (totalRead / (totalTime.toDouble() / 1_000_000_000.0)).toLong(),
                )
            }
        }
    }
}
