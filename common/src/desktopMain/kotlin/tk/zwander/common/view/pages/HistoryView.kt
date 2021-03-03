package tk.zwander.common.view.pages

import com.soywiz.klock.DateTime
import org.jsoup.Jsoup
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.util.makeFirmwareString

actual object PlatformHistoryView {
    actual suspend fun parseHistory(body: String): List<HistoryInfo> {
        val doc = Jsoup.parse(body)
        val table = doc.selectFirst(".firmware-responsive-table")
        val rows = table.select(".firmware-latest-item")

        return rows.map {
            val spans = it.select("span")
            val date = spans[0].text()
            val version = spans[1].text()
            val pda = spans[2].text()
            val csc = spans[3].text()

            HistoryInfo(
                DateTime.parse(date),
                version,
                makeFirmwareString(pda, csc)
            )
        }
    }
}