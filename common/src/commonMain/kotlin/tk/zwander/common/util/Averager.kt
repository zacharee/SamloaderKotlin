package tk.zwander.common.util

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.toDuration

class Averager(private val timeThreshold: Duration = 1000L.toDuration(DurationUnit.MILLISECONDS)) {
    data class ChunkData(
        val duration: Duration,
        val read: Long,
        val currentTime: TimeSource.Monotonic.ValueTimeMark,
    )

    private val mutex = Mutex()
    private val chunk = ArrayDeque<ChunkData>(30)
    private val sum = atomic(Pair(Duration.ZERO, 0L))

    suspend fun updateAndSum(duration: Duration, read: Long): Pair<Duration, Long> {
        return mutex.withLock {
            unsafeUpdate(duration, read)
            sum.value
        }
    }

    suspend fun close() {
        mutex.withLock {
            chunk.clear()
        }
    }

    private fun unsafeUpdate(duration: Duration, read: Long) {
        val currentTime = currentTime()
        chunk.addFirst(ChunkData(duration, read, currentTime))

        var remDur = Duration.ZERO
        var remRead = 0L

        while ((currentTime - chunk.last().currentTime) > timeThreshold) {
            val removed = chunk.removeLast()

            remDur += removed.duration
            remRead += removed.read
        }

        sum.value = sum.value.copy(
            first = sum.value.first + duration - remDur,
            second = sum.value.second + read - remRead,
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun currentTime(): TimeSource.Monotonic.ValueTimeMark {
        return TimeSource.Monotonic.markNow()
    }
}
