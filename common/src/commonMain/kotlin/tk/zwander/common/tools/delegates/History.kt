package tk.zwander.common.tools.delegates

import com.fleeksoft.ksoup.Ksoup
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.util.ChangelogHandler
import tk.zwander.common.util.CrossPlatformBugsnag
import tk.zwander.common.util.firstElementByTagName
import tk.zwander.common.util.getFirmwareHistoryString
import tk.zwander.common.util.getFirmwareHistoryStringFromSamsung
import tk.zwander.common.util.invoke
import tk.zwander.common.util.makeFirmwareString
import tk.zwander.commonCompose.model.HistoryModel
import tk.zwander.samloaderkotlin.resources.MR

object History {
    private fun parseHistoryXml(xml: String): List<HistoryInfo> {
        val doc = Ksoup.parse(xml)

        val latest = doc.firstElementByTagName("firmware")?.firstElementByTagName("version")?.firstElementByTagName("latest")
        val historical = doc.firstElementByTagName("firmware")?.firstElementByTagName("version")?.firstElementByTagName("upgrade")
            ?.getElementsByTag("value")

        val items = arrayListOf<HistoryInfo>()

        fun parseFirmware(string: String): String {
            val firmwareParts = string.split("/").filterNot { it.isBlank() }.toMutableList()

            if (firmwareParts.size == 2) {
                firmwareParts.add(firmwareParts[0])
                firmwareParts.add(firmwareParts[0])
            }

            if (firmwareParts.size == 3) {
                firmwareParts.add(firmwareParts[0])
            }

            return firmwareParts.joinToString("/")
        }

        latest?.apply {
            val androidVersion = latest.attribute("o")?.value

            items.add(
                HistoryInfo(
                    date = null,
                    androidVersion = androidVersion,
                    firmwareString = parseFirmware(latest.text())
                )
            )
        }

        historical?.apply {
            items.addAll(
                mapNotNull {
                    val firmware = parseFirmware(it.text())

                    if (firmware.isNotBlank()) {
                        HistoryInfo(
                            date = null,
                            androidVersion = null,
                            firmwareString = firmware
                        )
                    } else {
                        null
                    }
                }.sortedByDescending {
                    it.firmwareString.let { f ->
                        f.substring(f.lastIndex - 3)
                    }
                }
            )
        }

        return items
    }

    private fun parseHistory(body: String): List<HistoryInfo> {
        val doc = Ksoup.parse(body)

        val listItems = doc.select("a[class*=\"firmwareTable_flexRow_\"]")

        return listItems.map {
            val cols = it.select("div[class*=\"firmwareTable_flexCell\"]")
            val date = cols[5].text().split(" ").last()
            val version = cols[4].text().split(" ").last()

            val link = it.attr("href")
            val split = link.split("/")

            val pda = split[split.lastIndex - 1]
            val csc = split[split.lastIndex]

            val formats = arrayOf(
                DateTimeComponents.Format {
                    year()
                    char('/')
                    monthNumber(Padding.NONE)
                    char('/')
                    dayOfMonth(Padding.NONE)
                },
                DateTimeComponents.Format {
                    year()
                    char('-')
                    monthNumber(Padding.NONE)
                    char('-')
                    dayOfMonth(Padding.NONE)
                },
                DateTimeComponents.Format {
                    monthNumber(Padding.NONE)
                    char('/')
                    dayOfMonth(Padding.NONE)
                    char('/')
                    year()
                },
            )

            val parsed = formats.firstNotNullOfOrNull { format ->
                try {
                    format.parse(date).toLocalDate()
                } catch (e: Exception) {
                    null
                }
            }

            HistoryInfo(
                parsed ?: throw IllegalArgumentException("Invalid date format $date"),
                version,
                makeFirmwareString(pda, csc),
            )
        }
    }

    suspend fun onFetch(model: HistoryModel) {
        try {
            val historyString = getFirmwareHistoryString(model.model.value, model.region.value)
            val historyStringXml = getFirmwareHistoryStringFromSamsung(model.model.value, model.region.value)

            if (historyString == null && historyStringXml == null) {
                model.endJob(MR.strings.historyError())
            } else {
                try {
                    val parsed = when {
                        historyString != null -> {
                            parseHistory(historyString)
                        }

                        historyStringXml != null -> {
                            parseHistoryXml(historyStringXml)
                        }

                        else -> {
                            model.endJob(MR.strings.historyError())
                            return
                        }
                    }

                    model.changelogs.value = try {
                        ChangelogHandler.getChangelogs(
                            model.model.value,
                            model.region.value
                        )
                    } catch (e: Exception) {
                        println("Error retrieving changelogs")
                        e.printStackTrace()
                        null
                    }
                    model.historyItems.value = parsed.distinctBy { it.firmwareString }
                    model.endJob("")
                } catch (e: Exception) {
                    e.printStackTrace()
                    model.endJob(MR.strings.historyErrorFormat(e.message.toString()))
                }
            }
        } catch (e: Throwable) {
            CrossPlatformBugsnag.notify(e)

            model.endJob("${MR.strings.historyError()}${e.message?.let { "\n\n$it" }}")
        }
    }
}