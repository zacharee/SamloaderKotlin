package tk.zwander.common.tools

import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream
import io.ktor.utils.io.*
import kotlinx.coroutines.*
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
     * @param progressCallback a callback to keep track of th®e download.
     */
    @OptIn(ExperimentalTime::class)
    suspend fun download(response: AsyncInputStream, size: Long, output: AsyncOutputStream, outputSize: Long, progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit) {
        withContext(Dispatchers.Default) {
            val chunkSize = 0x300000

            val read = ByteArray(chunkSize)

            if (outputSize < size) {
                var len: Int
                var totalLen = 0L

                val averager = Averager()

                while (isActive) {
                    val nano = measureTime {
                        len = response.read(read, 0, chunkSize)
                        totalLen += len

                        if (len > 0) {
                            output.write(read, 0, len)
                        }
                    }.inWholeNanoseconds

                    if (len <= 0) break

                    val lenF = len
                    val totalLenF = totalLen

                    async {
                        averager.update(nano, lenF.toLong())
                        val (totalTime, totalRead, _) = averager.sum()

                        launch(start = CoroutineStart.UNDISPATCHED) {
                            progressCallback(
                                totalLenF + outputSize,
                                size,
                                (totalRead / (totalTime.toDouble() / 1_000_000_000.0)).toLong()
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
