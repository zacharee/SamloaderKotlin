package tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.jdom2.filter.Filters
import org.jdom2.input.SAXBuilder
import java.io.*
import java.security.MessageDigest
import java.security.Security
import java.util.zip.CRC32
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

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

    suspend fun decryptProgress(inf: File, outf: FileOutputStream, key: ByteArray, length: Long, progressCallback: (current: Long, max: Long) -> Unit) {
        withContext(Dispatchers.IO) {
            val cipher = Cipher.getInstance("AES/ECB/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))

            val input = RandomAccessFile(inf, "r")

            assert(length % 16 == 0L)

            val buffer = ByteArray(4096)

            outf.use { writer ->
                var len: Int
                var count = 0L
                while (true) {
                    len = input.read(buffer)
                    count += len
                    if (len <= 0) break

                    val decBlock = cipher.doFinal(buffer, 0, len)

                    writer.write(decBlock, 0, decBlock.size)
                    progressCallback(count, length)
                }
            }

            input.close()
        }
    }

    suspend fun checkCrc32(enc: File, expected: Long, progressCallback: (current: Long, max: Long) -> Unit): Boolean {
        val crc = CRC32()

        withContext(Dispatchers.IO) {
            DataInputStream(enc.inputStream()).use { input ->
                runBlocking(Dispatchers.IO) {
                    val buffer = ByteArray(0x10000)

                    input.use { d ->
                        var len: Int
                        var count = 0L

                        while (true) {
                            len = d.read(buffer)
                            count += len

                            if (len <= 0) break

                            crc.update(buffer, 0, len)
                            progressCallback(count, enc.length())
                        }
                    }
                }
            }
        }

        return crc.value == expected
    }
}