package tk.zwander.commonCompose.view.pages

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import org.jsoup.Jsoup
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.util.makeFirmwareString

actual object PlatformHistoryView {
    actual suspend fun parseHistory(body: String): List<HistoryInfo> {
        val doc = Jsoup.parse(body)

        val table = doc.selectFirst("#flist")
        val rows = table!!.children()[0].children().run { subList(1, size) }

        return rows.map {
            val cols = it.select("td")
            val date = cols[5].text()
            val version = cols[4].text()

            val link = cols[7].children().attr("href")
            val split = link.split("-")

            val pda = split[split.lastIndex - 1]
            val csc = split[split.lastIndex].split(".")[0]

            val formats = arrayOf(
                "yyyy/M/d",
                "yyyy-M-d"
            )

            val parsed = formats.mapNotNull { format ->
                try {
                    DateFormat(format).tryParse(date)
                } catch (e: Exception) {
                    null
                }
            }.firstOrNull()

            HistoryInfo(
                parsed ?: throw IllegalArgumentException("Invalid date format $date"),
                version,
                makeFirmwareString(pda, csc)
            )
        }
    }
}