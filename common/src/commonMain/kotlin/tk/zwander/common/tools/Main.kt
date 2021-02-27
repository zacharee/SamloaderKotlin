package tk.zwander.common.tools

import io.ktor.utils.io.core.internal.*

@DangerousInternalIoApi
expect object PlatformMain {
    fun getBinaryInfo(response: String): Main.BinaryFileInfo
}

@DangerousInternalIoApi
class Main {
    data class BinaryFileInfo(
        val path: String,
        val fileName: String,
        val size: Long,
        val crc32: Long?
    )

    suspend fun getBinaryFile(client: FusClient, fw: String, model: String, region: String): BinaryFileInfo {
        val request = Request.binaryInform(fw, model, region, client.nonce)
        val response = client.makeReq("NF_DownloadBinaryInform.do", request)

        return PlatformMain.getBinaryInfo(response)
    }
}