package tk.zwander.common.tools

import co.touchlab.stately.isolate.IsolateState
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import tk.zwander.common.util.Averager
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Manage downloading firmware.
 */
object Downloader {
    /**
     * Download an encrypted firmware file given an input and output.
     * @param response a stream of the firmware from Samsung's server.
     * @param size the size of the download.
     * @param output where to save the file.
     * @param outputSize the current size of the output file. Used for resuming downloads.
     * @param progressCallback a callback to keep track of the download.
     */
    @OptIn(ExperimentalTime::class)
    suspend fun download(response: AsyncInputStream, size: Long, output: AsyncOutputStream, outputSize: Long, progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit) {
        coroutineScope {
            withContext(Dispatchers.Unconfined) {
                val chunkSize = 0x300000

                val read = ByteArray(chunkSize)

                if (outputSize >= size) {
                    return@withContext
                }

                var len: Int
                var totalLen = 0L

                val averager = Averager()

                try {
                    while (isActive) {
                        val nano = measureTime {
                            len = response.read(read, 0, chunkSize)
                            totalLen += len

                            if (len > 0) {
                                output.write(read, 0, len)
                            }
                        }.toLongNanoseconds()

                        if (len <= 0) break

                        val lenF = len
                        val totalLenF = totalLen

                        async {
                            averager.update(nano, lenF.toLong())
                            val (totalTime, totalRead, _) = averager.sum()

                            this@coroutineScope.launchImmediately {
                                progressCallback(
                                    totalLenF + outputSize,
                                    size,
                                    (totalRead / (totalTime.toDouble() / 1_000_000_000.0)).toLong()
                                )
                            }
                        }
                    }
                } finally {
                    response.close()
                    output.close()
                }
            }
        }
    }
}