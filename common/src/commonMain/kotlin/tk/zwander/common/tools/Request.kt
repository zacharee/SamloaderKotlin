package tk.zwander.common.tools

import io.ktor.utils.io.core.internal.*
import tk.zwander.common.data.BinaryFileInfo

/**
 * Delegate XML creation to the platform until there's an MPP library.
 */
expect object PlatformRequest {
    fun createBinaryInform(fw: String, model: String, region: String, nonce: String): String
    fun createBinaryInit(fileName: String, nonce: String): String
    fun getBinaryInfo(response: String): BinaryFileInfo
}

/**
 * Handle some requests to Samsung's servers.
 */
@OptIn(DangerousInternalIoApi::class)
object Request {
    /**
     * Generate a logic-check for a given input.
     * @param input the value that needs a logic-check.
     * @param nonce the nonce used to generate the logic-check.
     * @return the logic-check.
     */
    fun getLogicCheck(input: String, nonce: String): String {
        if (input.length < 16) {
            throw IllegalArgumentException("Input is too short")
        }

        return nonce.map { input[it.toInt() and 0xf] }.joinToString("")
    }

    /**
     * Generate the XML needed to perform a binary inform.
     * @param fw the firmware string.
     * @param model the device model.
     * @param region the device region.
     * @param nonce the session nonce.
     * @return the needed XML.
     */
    fun createBinaryInform(fw: String, model: String, region: String, nonce: String): String {
        return PlatformRequest.createBinaryInform(fw, model, region, nonce)
    }

    /**
     * Generate the XML needed to perform a binary init.
     * @param fileName the name of the firmware file.
     * @param nonce the session nonce.
     * @return the needed XML.
     */
    fun createBinaryInit(fileName: String, nonce: String): String {
        return PlatformRequest.createBinaryInit(fileName, nonce)
    }

    /**
     * Retrieve the file information for a given firmware.
     * @param client the FusClient used to request the data.
     * @param fw the firmware version string.
     * @param model the device model.
     * @param region the device region.
     * @return a BinaryFileInfo instance representing the file.
     */
    suspend fun getBinaryFile(client: FusClient, fw: String, model: String, region: String): BinaryFileInfo {
        val request = createBinaryInform(fw, model, region, client.nonce)
        val response = client.makeReq(FusClient.Request.BINARY_INFORM, request)

        return PlatformRequest.getBinaryInfo(response)
    }
}