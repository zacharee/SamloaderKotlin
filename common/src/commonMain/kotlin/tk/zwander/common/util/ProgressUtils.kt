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
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTimedValue

const val DEFAULT_CHUNK_SIZE = 1024 * 512

suspend fun streamOperationWithProgress(
    input: Source,
    output: Sink,
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
            val len = input.readAtMostTo(buffer, 0, buffer.size)

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

@OptIn(DelicateCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
suspend fun trackOperationProgress(
    size: Long,
    progressCallback: suspend (current: Long, max: Long, bps: Long) -> Unit,
    operation: suspend () -> Long,
    progressOffset: Long = 0L,
    condition: () -> Boolean = { true },
    throttle: Boolean = true,
) {
    coroutineScope {
        val totalLen = atomic(0L)
        val finished = atomic(false)
        val lastUpdateTime = atomic(Instant.DISTANT_PAST)

        val averager = Averager()

        while (isActive && condition()) {
            val timedValue = measureTimedValue {
                operation()
            }

            if (timedValue.value <= 0) break

            GlobalScope.launch(Dispatchers.Unconfined) {
                val len = timedValue.value
                totalLen += len

                val nano = timedValue.duration.inWholeNanoseconds
                val current = totalLen.value + progressOffset

                if (finished.value || current >= size) {
                    return@launch
                }

                val (totalTime, totalRead) = averager.updateAndSum(nano, len)
                val bps = (totalRead / (totalTime.toDouble() / 1_000_000_000.0)).toLong()

                if (throttle) {
                    val currentTime = Clock.System.now()
                    if (currentTime - lastUpdateTime.value < 100.milliseconds) {
                        return@launch
                    }
                    lastUpdateTime.value = currentTime
                }

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
        averager.close()
    }
}
