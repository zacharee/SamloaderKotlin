package tk.zwander.common.data.imei

import korlibs.io.serialization.csv.CSV
import tk.zwander.samloaderkotlin.resources.MR

data object IMEIGenerator {
    fun makeImeiForModel(model: String): String? {
        val adjustedModel = if (model.endsWith("U1")) {
            model.replace("U1", "U")
        } else {
            model
        }

        val tac = IMEIDatabase.imeis[adjustedModel] ?: return null
        val baseImei = "${tac}${IMEIDatabase.DUMMY_SERIAL}"

        return calculateCheckDigitForPartialImei(baseImei)
    }

    private fun calculateCheckDigitForFullImei(imei: String): Int {
        val length = imei.length
        val parity = length % 2

        var sum = 0

        for (i in length - 1 downTo 0) {
            var d = imei[i].digitToInt()

            if (i % 2 == parity) {
                d *= 2
            }

            if (d > 9) {
                d -= 9
            }

            sum += d
        }

        return sum % 10
    }

    private fun calculateCheckDigitForPartialImei(baseImei: String): String {
        val check = calculateCheckDigitForFullImei("${baseImei}0")

        val adjustedCheck = if (check == 0) 0 else 10 - check

        return "${baseImei}${adjustedCheck}"
    }
}

data object IMEIDatabase {
    const val DUMMY_SERIAL = "123456"

    val imeis = mutableMapOf<String, String>()

    init {
        val csv = CSV.parse(MR.files.tacs.readText())
        csv.lines.forEach { line ->
            if (line.size >= 2) {
                val tac = line[0]
                val model = line[1].cleanUpModel()
                val model2 = line.getOrNull(2)?.cleanUpModel()

                imeis[model] = tac
                model2?.let { imeis[it] = tac }
            }
        }
    }

    private fun String.cleanUpModel(): String {
        val trimmed = trim()

        if (!trimmed.contains("-")) {
            return trimmed
        }

        val firstSpace = trimmed.indexOfFirst { it == ' ' }.takeIf { it != -1 } ?: trimmed.length
        val beforeSpaces = trimmed.slice(0 until firstSpace)

        val firstSlash = beforeSpaces.indexOfFirst { it == '/' }.takeIf { it != -1 } ?: beforeSpaces.length

        return beforeSpaces.slice(0 until firstSlash)
    }
}
