package tk.zwander.common

fun main(args: Array<String>) {
    val nonce = "ltz306vrvbgs2jd9"
    val fw = "S918U1UEU1BWK4/S918U1OYM1BWK4/S918U1UEU1BWK4/S918U1UEU1BWK4"

    val samFirmResult = getLogicCheckSamFirm(fw, nonce)
    val oldResult = getLogicCheckOld(fw, nonce)

    println("SamFirm: $samFirmResult")
    println("Old: $oldResult")
}

fun getLogicCheckSamFirm(input: String, nonce: String): String {
    if (input.length < 16) {
        return ""
    }

    val stringBuilder = StringBuilder()
    var num1 = 0

    if (input.endsWith(".zip.enc2") || input.endsWith(".zip.enc4")) {
        num1 = input.length - 25
    }

    nonce.forEach { num2 ->
        val num3 = num2.code and 15

        if (input.length <= num3 + num1) {
            return ""
        }

        stringBuilder.append(input[num3 + num1])
    }

    return stringBuilder.toString()
}

fun getLogicCheckOld(input: String, nonce: String): String {
    return buildString {
        nonce.forEach {char ->
            append(input[char.code and 0xf])
        }
    }}
