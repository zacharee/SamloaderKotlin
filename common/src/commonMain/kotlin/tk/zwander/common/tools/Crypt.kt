package tk.zwander.common.tools

import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream
import com.soywiz.korio.stream.openAsync
import com.soywiz.korio.util.checksum.CRC32
import com.soywiz.korio.util.checksum.checksum
import com.soywiz.krypto.AES
import com.soywiz.krypto.MD5
import com.soywiz.krypto.Padding
import com.soywiz.krypto.encoding.hex
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.io.core.Input
import kotlinx.io.core.Output
import kotlinx.io.core.toByteArray
import kotlin.time.*

expect object PlatformCrypt {
    fun getFwAndLogic(response: String): Pair<String, String>
}

@DangerousInternalIoApi
@ExperimentalTime
object Crypt {
    suspend fun getV4Key(version: String, model: String, region: String): ByteArray {
        val client = FusClient()
        val request = Request.binaryInform(version, model, region, client.nonce)
        val response = client.makeReq("NF_DownloadBinaryInform.do", request)

        val (fwVer, logicVal) = PlatformCrypt.getFwAndLogic(response)
        val decKey = Request.getLogicCheck(fwVer, logicVal)

        return MD5.digest(decKey.toByteArray()).bytes
    }

    suspend fun getV2Key(version: String, model: String, region: String): ByteArray {
        val decKey = "${region}:${model}:${version}"
        return MD5.digest(decKey.toByteArray()).bytes
    }

    suspend fun decryptProgress(inf: AsyncInputStream, outf: AsyncOutputStream, key: ByteArray, length: Long, progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit) {
        coroutineScope {
            withContext(Dispatchers.Default) {
                val buffer = ByteArray(0x300000)

                var len: Int
                var count = 0L

                val chunk = ArrayList<Triple<Long, Long, Long>>(1000)

                while (this.isActive) {
                    val nano = measureTime {
                        len = inf.read(buffer, 0, buffer.size)
                        count += len

                        if (len > 0) {
                            val decBlock = AES.decryptAesEcb(buffer.sliceArray(0 until len), key, Padding.NoPadding)

                            outf.write(decBlock, 0, decBlock.size)
                        }
                    }.toLongNanoseconds()

                    if (len <= 0) break

                    val currentNano = Clock.System.now().toEpochMilliseconds() * 1000 * 1000
                    chunk.add(Triple(nano, len.toLong(), currentNano))

                    chunk.removeAll { currentNano - it.third > 1000 * 1000 * 1000 }

                    val timeAvg = chunk.sumOf { it.first }
                    val lenAvg = chunk.sumOf { it.second }

                    async {
                        progressCallback(
                            count,
                            length,
                            (lenAvg / (timeAvg.toDouble() / 1000.0 / 1000.0 / 1000.0)).toLong()
                        )
                    }
                }

                println("read $count, actual $length")

                inf.close()
                outf.close()
            }
        }
    }

    suspend fun checkCrc32(enc: AsyncInputStream, encSize: Long, expected: Long, progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit): Boolean {
        var crcVal = CRC32.initialValue

        coroutineScope {
            withContext(Dispatchers.Default) {
                val buffer = ByteArray(0x300000)

                var len: Int
                var count = 0L

                val chunk = ArrayList<Triple<Long, Long, Long>>(1000)

                while (isActive) {
                    val nano = measureTime {
                        len = enc.read(buffer, 0, buffer.size)
                        count += len

                        if (len > 0) {
                            crcVal = CRC32.update(crcVal, buffer, 0, len)
                        }
                    }.toLongNanoseconds()

                    if (len <= 0) break

                    val currentNano = Clock.System.now().toEpochMilliseconds() * 1000 * 1000

                    chunk.add(Triple(nano, len.toLong(), currentNano))
                    chunk.removeAll { currentNano - it.third > 1000 * 1000 * 1000 }

                    val timeAvg = chunk.sumOf { it.first }
                    val lenAvg = chunk.sumOf { it.second }

                    async {
                        progressCallback(count, encSize, (lenAvg / (timeAvg.toDouble() / 1000.0 / 1000.0 / 1000.0)).toLong())
                    }
                }
            }
        }

        enc.close()

        return crcVal == expected.toInt()
    }
}