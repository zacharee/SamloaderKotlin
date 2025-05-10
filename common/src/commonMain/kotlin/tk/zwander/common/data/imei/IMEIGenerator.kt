@file:OptIn(DelicateCoroutinesApi::class)

package tk.zwander.common.data.imei

import de.halfbit.csv.Csv
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import tk.zwander.common.util.globalHttpClient
import tk.zwander.common.util.invoke
import tk.zwander.samloaderkotlin.resources.MR

data object IMEIGenerator {
    fun makeImeisForModel(
        model: String?,
        imeis: Map<String, Set<String>> = IMEIDatabase.tacs.value,
    ): List<String> {
        val tacs = imeis[model] ?: return emptyList()

        return IMEIDatabase.DUMMY_SERIALS.flatMap { serial ->
            tacs.map { tac ->
                val baseImei = "${tac}${serial}"
                calculateCheckDigitForPartialImei(baseImei)
            }
        }
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
    val DUMMY_SERIALS = arrayOf(
        "123456",
        "012345",
        "011111",
        "005555",
        "020202",
    )
    private const val LIVE_ENDPOINT =
        "https://raw.githubusercontent.com/zacharee/SamloaderKotlin/master/common/src/commonMain/moko-resources/files/tacs.csv"

    val tacs = MutableStateFlow<Map<String, Set<String>>>(mapOf())

    init {
        GlobalScope.launch(Dispatchers.IO) {
            loadLocalCsv()
            try {
                val response = globalHttpClient.get(LIVE_ENDPOINT)
                val liveData = response.bodyAsText()

                if (!response.status.isSuccess()) {
                    error(response.status)
                }

                if (liveData.isNotBlank()) {
                    loadCsv(liveData)
                }
            } catch (e: Throwable) {
                println("Failed to fetch remote TAC list, using local resource instead. ${e.message}")
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

        val firstSlash =
            beforeSpaces.indexOfFirst { it == '/' }.takeIf { it != -1 } ?: beforeSpaces.length

        return beforeSpaces.slice(0 until firstSlash)
    }

    private fun loadLocalCsv() {
        loadCsv(MR.files.tacs_csv()!!.decodeToString())
    }

    private fun loadCsv(csvString: String) {
        val csv = Csv.parserCsvText(csvString)
        csv.allRows.forEach { line ->
            if (line.size >= 2) {
                val tac = line[0]
                val model = line[1].cleanUpModel()

                val newMap = tacs.value.toMutableMap()

                val model1Set = (newMap[model] ?: setOf()).toMutableSet()
                model1Set.add(tac)
                newMap[model] = model1Set

                if (line.size > 2) {
                    line.slice(2..line.lastIndex).forEach { secondaryModel ->
                        val cleaned = secondaryModel.cleanUpModel()
                        val secondarySet = (newMap[cleaned] ?: setOf()).toMutableSet()
                        secondarySet.add(tac)
                        newMap[cleaned] = secondarySet
                    }
                }

                tacs.value = newMap
            }
        }
    }
}
