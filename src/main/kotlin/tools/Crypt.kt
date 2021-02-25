package tools

import kotlinx.coroutines.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.jdom2.input.SAXBuilder
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.security.Security
import java.util.zip.CRC32
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.system.measureNanoTime

object Crypt {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    private val md5: MessageDigest = MessageDigest.getInstance("MD5")

    fun getV4Key(version: String, model: String, region: String): ByteArray {
        val client = FusClient()
        val request = Request.binaryInform(version, model, region, client.nonce)
        val response = client.makeReq("NF_DownloadBinaryInform.do", request)

        val doc = SAXBuilder().build(response.byteInputStream())
        val root = doc.rootElement
        val fwVer = root.getChild("FUSBody")
            .getChild("Results")
            .getChild("LATEST_FW_VERSION")
            .getChild("Data")
            .text
        val logicVal = root.getChild("FUSBody")
            .getChild("Put")
            .getChild("LOGIC_VALUE_FACTORY")
            .getChild("Data")
            .text

        val decKey = Request.getLogicCheck(fwVer, logicVal)

        return md5.digest(decKey.toByteArray())
    }

    fun getV2Key(version: String, model: String, region: String): ByteArray {
        val decKey = "${region}:${model}:${version}"
        return md5.digest(decKey.toByteArray())
    }

    suspend fun decryptProgress(inf: File, outf: FileOutputStream, key: ByteArray, length: Long, progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit) {
        coroutineScope {
            withContext(Dispatchers.IO) {
                val cipher = Cipher.getInstance("AES/ECB/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))

                val input = RandomAccessFile(inf, "r")

                assert(length % 16 == 0L)

                val buffer = ByteArray(4096)

                outf.use { writer ->
                    var len: Int
                    var count = 0L

                    val chunk = ArrayList<Triple<Long, Long, Long>>(1000)

                    while (this.isActive) {
                        val nano = measureNanoTime {
                            len = input.read(buffer)
                            count += len

                            if (len > 0) {
                                val decBlock = cipher.doFinal(buffer, 0, len)
                                writer.write(decBlock, 0, decBlock.size)
                            }
                        }

                        if (len <= 0) break

                        chunk.add(Triple(nano, len.toLong(), System.nanoTime()))

                        val current = System.nanoTime()
                        chunk.removeIf { current - it.third > 1000 * 1000 * 1000 }

                        val timeAvg = chunk.map { it.first }.sum()
                        val lenAvg = chunk.map { it.second }.sum()

                        async {
                            progressCallback(count, length, (lenAvg / (timeAvg.toDouble() / 1000.0 / 1000.0 / 1000.0)).toLong())
                        }
                    }
                }

                input.close()
            }
        }
    }

    suspend fun checkCrc32(enc: File, expected: Long, progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit): Boolean {
        val crc = CRC32()

        coroutineScope {
            withContext(Dispatchers.IO) {
                enc.inputStream().use { input ->
                    val buffer = ByteArray(0x10000)

                    input.use { d ->
                        var len: Int
                        var count = 0L

                        val chunk = ArrayList<Triple<Long, Long, Long>>(1000)

                        while (isActive) {
                            val nano = measureNanoTime {
                                len = d.read(buffer, 0, 0x10000)
                                count += len

                                if (len > 0) {
                                    crc.update(buffer, 0, len)
                                }
                            }

                            if (len <= 0) break

                            chunk.add(Triple(nano, len.toLong(), System.nanoTime()))
                            val current = System.nanoTime()
                            chunk.removeIf { current - it.third > 1000 * 1000 * 1000 }

                            val timeAvg = chunk.map { it.first }.sum()
                            val lenAvg = chunk.map { it.second }.sum()

                            async {
                                progressCallback(count, enc.length(), (lenAvg / (timeAvg.toDouble() / 1000.0 / 1000.0 / 1000.0)).toLong())
                            }
                        }
                    }
                }
            }
        }

        return crc.value == expected
    }
}