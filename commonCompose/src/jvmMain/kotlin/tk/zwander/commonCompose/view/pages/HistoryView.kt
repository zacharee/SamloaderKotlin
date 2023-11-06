package tk.zwander.commonCompose.view.pages

import com.soywiz.klock.DateFormat
import org.jsoup.Jsoup
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.util.makeFirmwareString

actual object PlatformHistoryView {
    actual suspend fun parseHistory(body: String): List<HistoryInfo> {
        val doc = Jsoup.parse(body)

        val listItems = doc.select(".index_list").apply {
            removeAt(0)
        }

        return listItems.map {
            val cols = it.select(".index_body_list")
            val date = cols[6].text()
            val version = cols[5].text()

            val link = cols[0].children()[0].children().attr("href")
            val split = link.split("-")

            val pda = split[split.lastIndex - 1]
            val csc = split[split.lastIndex].split(".")[0]

            val formats = arrayOf(
                "yyyy/M/d",
                "yyyy-M-d",
                "M/d/yyyy"
            )

            val parsed = formats.firstNotNullOfOrNull { format ->
                try {
                    DateFormat(format).tryParse(date)
                } catch (e: Exception) {
                    null
                }
            }

            HistoryInfo(
                parsed ?: throw IllegalArgumentException("Invalid date format $date"),
                version,
                makeFirmwareString(pda, csc)
            )
        }
    }
}
