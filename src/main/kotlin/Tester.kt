import kotlinx.coroutines.runBlocking
import tools.*
import util.MD5
import java.io.File

object Tester {
    @JvmStatic
    fun main(args: Array<String>) {
        val model = "SM-N986U"
        val region = "TMB"

        val latest = VersionFetch.getLatestVer(model, region)
        val client = FusClient()
        println(latest)

        val (path, fileName, size, crc32) = Main().getBinaryFile(client, latest, model, region)

        println("($path, $fileName, $size)")

        val request = Request.binaryInit(fileName, client.nonce)
        val resp = client.makeReq("NF_DownloadBinaryInitForMass.do", request)

        val output = File(fileName)
        val offset = if (output.exists()) output.length() else 0

        val response = client.downloadFile(path + fileName, offset)
        val md5 = if (response.headers().firstValue("Content-MD5").isPresent) response.headers().firstValue("Content-MD5").get() else null

        runBlocking {
            Downloader.download(response, size, output) { current, max, bps ->
                println("$current, $max, ${current.toFloat() / max * 100}%")
            }
        }

        if (crc32 != null) {
            val crcResult = runBlocking {
                Crypt.checkCrc32(output, crc32) { current, max, bps ->
                    println("${current.toFloat() / max * 100f}%")
                }
            }
        }

        if (md5 != null) {
            val result = MD5.checkMD5(md5, output)
            println("MD5 $result")
        }

//        val decFile = File(fileName.replace(".enc4", "").replace(".enc2", ""))
//        val key = if (fileName.endsWith(".enc2")) Crypt.getV2Key(latest, model, region) else Crypt.getV4Key(latest, model, region)
//
//        runBlocking {
//            Crypt.decryptProgress(output, decFile.outputStream(), key, output.length()) { current, max ->
//                println("Decrypting chunk $current of $max, ${current.toFloat() / max.toFloat() * 100}%")
//            }
//        }
    }
}