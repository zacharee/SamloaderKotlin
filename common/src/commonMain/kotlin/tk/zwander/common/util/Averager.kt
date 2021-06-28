package tk.zwander.common.util

import co.touchlab.stately.isolate.IsolateState
import com.soywiz.klock.DateTime
import io.ktor.util.date.*
import kotlinx.datetime.Clock

class Averager(initialCapacity: Int = 1000, private val thresholdNanos: Long = 1_000_000_000) {
    data class ChunkData(
        val durationNano: Long,
        val read: Long,
        val currentTimeNano: Long
    )

    private val chunk = IsolateState { ArrayList<ChunkData>(initialCapacity) }

    fun update(durationNano: Long, read: Long) {
        val currentTimeNano = currentTimeNano()
        chunk.access { chunk ->
            chunk.add(ChunkData(durationNano, read, currentTimeNano))
            chunk.removeAll { currentTimeNano - it.currentTimeNano > thresholdNanos }
        }
    }

    fun sum(): ChunkData {
        return chunk.access { chunk ->
            ChunkData(
                chunk.sumOf { it.durationNano },
                chunk.sumOf { it.read },
                currentTimeNano()
            )
        }
    }

    private fun currentTimeNano(): Long {
        return DateTime.now().unixMillisLong * 1_000_000
    }
}