package tk.zwander.common.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

class Averager(initialCapacity: Int = 1000, private val thresholdNanos: Long = 1_000_000_000) {
    data class ChunkData(
        val durationNano: Long,
        val read: Long,
        val currentTimeNano: Long
    )

    private val mutex = Mutex()
    private val chunk = ArrayList<ChunkData>(initialCapacity)

    suspend fun updateAndSum(durationNano: Long, read: Long): Pair<Long, Long> {
        return mutex.withLock {
            unsafeUpdate(durationNano, read)
            unsafeSum()
        }
    }

    suspend fun close() {
        mutex.withLock {
            chunk.clear()
        }
    }

    private fun unsafeUpdate(durationNano: Long, read: Long) {
        val currentTimeNano = currentTimeNano()
        chunk.add(ChunkData(durationNano, read, currentTimeNano))
        chunk.removeAll { currentTimeNano - it.currentTimeNano > thresholdNanos }
    }

    private fun unsafeSum(): Pair<Long, Long> {
        return (chunk.sumOf { it.durationNano } to
                chunk.sumOf { it.read })
    }

    private fun currentTimeNano(): Long {
        return Clock.System.now().toEpochMilliseconds() * 1_000_000
    }
}
