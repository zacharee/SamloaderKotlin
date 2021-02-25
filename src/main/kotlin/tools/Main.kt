package tools

import org.jdom2.input.SAXBuilder
import java.io.IOException

class Main {
    data class BinaryFileInfo(
        val path: String,
        val fileName: String,
        val size: Long,
        val crc32: Long
    )

    fun getBinaryFile(client: FusClient, fw: String, model: String, region: String): BinaryFileInfo {
        val request = Request.binaryInform(fw, model, region, client.nonce)
        val response = client.makeReq("NF_DownloadBinaryInform.do", request)

        val doc = SAXBuilder().build(response.byteInputStream())
        val root = doc.rootElement

        val status = root.getChild("FUSBody")
            .getChild("Results")
            .getChild("Status")
            .text.toInt()

        if (status != 200) {
            throw IOException("Bad return status: $status")
        }

        val size = root.getChild("FUSBody")
            .getChild("Put")
            .getChild("BINARY_BYTE_SIZE")
            .getChild("Data")
            .text.toLong()

        val fileName = root.getChild("FUSBody")
            .getChild("Put")
            .getChild("BINARY_NAME")
            .getChild("Data")
            .text

        val path = root.getChild("FUSBody")
            .getChild("Put")
            .getChild("MODEL_PATH")
            .getChild("Data")
            .text

        val crc32 = root.getChild("FUSBody")
            .getChild("Put")
            .getChild("BINARY_CRC")
            .getChild("Data")
            .text.toLong()

        return BinaryFileInfo(path, fileName, size, crc32)
    }
}