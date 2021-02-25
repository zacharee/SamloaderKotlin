package tools

import com.github.kittinunf.fuel.core.Response
import com.google.common.collect.EvictingQueue
import kotlinx.coroutines.*
import java.io.*
import java.net.http.HttpResponse
import java.util.*
import kotlin.system.measureNanoTime

object Downloader {
    suspend fun download(response: HttpResponse<InputStream>, size: Long, output: File, progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit) {
        coroutineScope {
            withContext(Dispatchers.IO) {
                val offset = if (output.exists()) output.length() else 0
                val chunkSize = 0x300000
                val input = BufferedInputStream(response.body())

                val read = ByteArray(chunkSize)

                FileOutputStream(output, true).use { writer ->
                    var len: Int
                    var totalLen = 0L

                    val chunk = Collections.synchronizedCollection(ArrayList<Triple<Long, Long, Long>>(1000))

                    while (isActive) {
                        val nano = measureNanoTime {
                            len = input.read(read, 0, chunkSize)
                            totalLen += len

                            if (len > 0) {
                                writer.write(read, 0, len)
                            }
                        }

                        if (len <= 0) break

                        val current = System.nanoTime()
                        val lenF = len
                        val totalLenF = totalLen

                        async {
                            chunk.add(Triple(nano, lenF.toLong(), current))
                            chunk.removeIf { current - it.third > 1000 * 1000 * 1000 }

                            val chunkSnapshot = ArrayList(chunk)

                            val timeAvg = chunkSnapshot.map { it.first }.sum()
                            val lenAvg = chunkSnapshot.map { it.second }.sum()

                            progressCallback(totalLenF + offset, size, (lenAvg / (timeAvg.toDouble() / 1000.0 / 1000.0 / 1000.0)).toLong())
                        }
                    }
                }

                input.close()
            }
        }
    }
}