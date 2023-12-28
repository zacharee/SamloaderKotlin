@file:OptIn(DelicateCoroutinesApi::class)

package tk.zwander.common.data.imei

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import korlibs.io.serialization.csv.CSV
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import tk.zwander.common.util.client
import tk.zwander.common.util.invoke
import tk.zwander.samloaderkotlin.resources.MR

data object IMEIGenerator {
    fun makeImeiForModel(model: String, imeis: Map<String, String> = IMEIDatabase.imeis.value): String? {
        val adjustedModel = if (model.endsWith("U1")) {
            model.replace("U1", "U")
        } else {
            model
        }

        val tac = imeis[adjustedModel] ?: return null
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
    const val LIVE_ENDPOINT = "https://raw.githubusercontent.com/zacharee/SamloaderKotlin/master/common/src/commonMain/resources/MR/files/tacs.csv"

    val imeis = MutableStateFlow<Map<String, String>>(mapOf())

    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = client.get(LIVE_ENDPOINT)
                val liveData = response.bodyAsText()

                if (liveData.isBlank()) {
                    loadLocalCsv()
                } else {
                    loadCsv(liveData)
                }
            } catch (e: Throwable) {
                println("Failed to fetch remote TAC list, using local resource instead. ${e.message}")
                loadLocalCsv()
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

    private fun loadLocalCsv() {
        loadCsv(MR.files.tacs()!!.decodeToString())
    }

    private fun loadCsv(csvString: String) {
        val csv = CSV.parse(csvString)
        csv.lines.forEach { line ->
            if (line.size >= 2) {
                val tac = line[0]
                val model = line[1].cleanUpModel()
                val model2 = line.getOrNull(2)?.cleanUpModel()

                val newValue = imeis.value.toMutableMap()
                newValue[model] = tac
                model2?.let { newValue[it] = tac }

                imeis.value = newValue
            }
        }
    }
}
