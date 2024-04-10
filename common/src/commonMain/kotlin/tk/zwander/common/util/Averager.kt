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

    private val chunkMutex = Mutex()
    private val chunk = ArrayList<ChunkData>(initialCapacity)

    suspend fun update(durationNano: Long, read: Long) {
        val currentTimeNano = currentTimeNano()
        chunkMutex.withLock {
            chunk.add(ChunkData(durationNano, read, currentTimeNano))
            chunk.removeAll { currentTimeNano - it.currentTimeNano > thresholdNanos }
        }
    }

    suspend fun sum(): ChunkData {
        return chunkMutex.withLock {
            ChunkData(
                chunk.sumOf { it.durationNano },
                chunk.sumOf { it.read },
                currentTimeNano()
            )
        }
    }

    private fun currentTimeNano(): Long {
        return Clock.System.now().toEpochMilliseconds() * 1_000_000
    }
}
