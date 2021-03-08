package tk.zwander.common.tools

import io.ktor.utils.io.core.internal.*

/**
 * Delegate XML parsing to the platform until there's an MPP library.
 */
@DangerousInternalIoApi
expect object PlatformMain {
    fun getBinaryInfo(response: String): Main.BinaryFileInfo
}

/**
 * Handle getting the information about the firmware file for a specific
 * model, region, and firmware combination.
 */
@DangerousInternalIoApi
object Main {
    /**
     * Represents a binary file to download.
     */
    data class BinaryFileInfo(
        val path: String,
        val fileName: String,
        val size: Long,
        val crc32: Long?
    )

    /**
     * Retrieve the file information for a given firmware.
     * @param client the FusClient used to request the data.
     * @param fw the firmware version string.
     * @param model the device model.
     * @param region the device region.
     * @return a BinaryFileInfo instance representing the file.
     */
    suspend fun getBinaryFile(client: FusClient, fw: String, model: String, region: String): BinaryFileInfo {
        val request = Request.binaryInform(fw, model, region, client.nonce)
        val response = client.makeReq("NF_DownloadBinaryInform.do", request)

        return PlatformMain.getBinaryInfo(response)
    }
}