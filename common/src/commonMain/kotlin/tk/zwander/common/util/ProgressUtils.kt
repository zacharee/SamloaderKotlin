package tk.zwander.common.util

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import okio.BufferedSink
import okio.BufferedSource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTimedValue

const val DEFAULT_CHUNK_SIZE = 1024 * 512

suspend fun streamOperationWithProgress(
    input: BufferedSource,
    output: BufferedSink,
    size: Long,
    progressCallback: suspend (current: Long, max: Long, bps: Long) -> Unit,
    operation: (suspend (buffer: ByteArray) -> ByteArray)? = null,
    chunkSize: Int = DEFAULT_CHUNK_SIZE,
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

                val result = operation?.invoke(exactData) ?: exactData

                output.write(result, 0, result.size)
            }

            len.toLong()
        },
        progressOffset = progressOffset,
    )

    withContext(Dispatchers.IO) {
        input.close()
        output.close()
    }
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun trackOperationProgress(
    size: Long,
    progressCallback: suspend (current: Long, max: Long, bps: Long) -> Unit,
    operation: suspend () -> Long,
    progressOffset: Long = 0L,
    condition: () -> Boolean = { true },
    throttle: Boolean = true,
) {
    coroutineScope {
        val len = atomic(0L)
        val totalLen = atomic(0L)
        val finished = atomic(false)
        val lastUpdateTime = atomic(Instant.DISTANT_PAST)

        val averager = Averager()

        while (isActive && condition()) {
            val timedValue = measureTimedValue {
                operation()
            }

            len.value = timedValue.value
            totalLen += timedValue.value

            val nano = timedValue.duration.inWholeNanoseconds

            if (len.value <= 0) break

            if (throttle) {
                val currentTime = Clock.System.now()
                if (currentTime - lastUpdateTime.value < 50.milliseconds) {
                    continue
                }
                lastUpdateTime.value = currentTime
            }

            GlobalScope.launch {
                val current = totalLen.value + progressOffset

                if (finished.value || current >= size) {
                    return@launch
                }

                val (totalTime, totalRead) = averager.updateAndSum(nano, len.value)
                val bps = (totalRead / (totalTime.toDouble() / 1_000_000_000.0)).toLong()

                if (finished.value) {
                    return@launch
                }

                progressCallback(
                    current,
                    size,
                    bps,
                )
            }
        }

        finished.value = true
    }
}
