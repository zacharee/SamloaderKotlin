package tk.zwander.common.tools

import co.touchlab.stately.isolate.IsolateState
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.io.core.Output
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

object Downloader {
    @OptIn(ExperimentalTime::class)
    suspend fun download(response: AsyncInputStream, size: Long, output: AsyncOutputStream, outputSize: Long, progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit) {
        coroutineScope {
            withContext(Dispatchers.Default) {
                val chunkSize = 0x300000

                val read = ByteArray(chunkSize)

                if (outputSize >= size) {
                    return@withContext
                }

                var len: Int
                var totalLen = 0L

                val chunk = IsolateState { ArrayList<Triple<Long, Long, Long>>(1000) }

                while (isActive) {
                    val nano = measureTime {
                        len = response.read(read, 0, chunkSize)
                        totalLen += len

                        if (len > 0) {
                            output.write(read, 0, len)
                        }
                    }.toLongNanoseconds()

                    if (len <= 0) break

                    val currentNano = Clock.System.now().toEpochMilliseconds() * 1000 * 1000
                    val lenF = len
                    val totalLenF = totalLen

                    async {
                        chunk.access { chunk ->
                            chunk.add(Triple(nano, lenF.toLong(), currentNano))
                            chunk.removeAll { currentNano - it.third > 1000 * 1000 * 1000 }

                            val chunkSnapshot = ArrayList(chunk)

                            val timeAvg = chunkSnapshot.sumOf { it.first }
                            val lenAvg = chunkSnapshot.sumOf { it.second }

                            this@coroutineScope.launchImmediately {
                                progressCallback(
                                    totalLenF + outputSize,
                                    size,
                                    (lenAvg / (timeAvg.toDouble() / 1000.0 / 1000.0 / 1000.0)).toLong()
                                )
                            }
                        }
                    }
                }

                response.close()
                output.close()
            }
        }
    }
}