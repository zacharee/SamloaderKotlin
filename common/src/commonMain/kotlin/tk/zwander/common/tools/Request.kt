package tk.zwander.common.tools

expect object PlatformRequest {
    fun binaryInform(fw: String, model: String, region: String, nonce: String): String
    fun binaryInit(fileName: String, nonce: String): String
}

object Request {
    fun getLogicCheck(input: String, nonce: String): String {
        if (input.length < 16) {
            throw IllegalArgumentException("Input is too short")
        }

        return nonce.map { input[it.toInt() and 0xf] }.joinToString("")
    }

    fun binaryInform(fw: String, model: String, region: String, nonce: String): String {
        return PlatformRequest.binaryInform(fw, model, region, nonce)
    }

    fun binaryInit(fileName: String, nonce: String): String {
        return PlatformRequest.binaryInit(fileName, nonce)
    }
}