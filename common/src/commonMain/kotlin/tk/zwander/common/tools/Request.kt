package tk.zwander.common.tools

/**
 * Delegate XML creation to the platform until there's an MPP library.
 */
expect object PlatformRequest {
    fun binaryInform(fw: String, model: String, region: String, nonce: String): String
    fun binaryInit(fileName: String, nonce: String): String
}

/**
 * Handle some request to Samsung's servers.
 */
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
    fun binaryInform(fw: String, model: String, region: String, nonce: String): String {
        return PlatformRequest.binaryInform(fw, model, region, nonce)
    }

    /**
     * Generate the XML needed to perform a binary init.
     * @param fileName the name of the firmware file.
     * @param nonce the session nonce.
     * @return the needed XML.
     */
    fun binaryInit(fileName: String, nonce: String): String {
        return PlatformRequest.binaryInit(fileName, nonce)
    }
}