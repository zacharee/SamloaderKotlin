package tk.zwander.commonCompose.view.pages

import com.soywiz.klock.DateFormat
import org.w3c.dom.Element
import org.w3c.dom.asList
import org.w3c.dom.get
import org.w3c.dom.parsing.DOMParser
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.util.makeFirmwareString

/**
 * Delegate HTML parsing to the platform until there's an MPP library.
 */
actual object PlatformHistoryView {
    actual suspend fun parseHistory(body: String): List<HistoryInfo> {
        val parser = DOMParser()
        val doc = parser.parseFromString(body, "text/html")

        val listItems = doc.querySelectorAll(".index_list").asList().toMutableList().apply {
            removeAt(0)
        }

        return listItems.mapNotNull {
            val cols = (it as? Element)?.querySelectorAll(".index_body_list") ?: return@mapNotNull null
            val date = cols[6]?.textContent ?: return@mapNotNull null
            val version = cols[5]?.textContent ?: return@mapNotNull null

            val link = cols[0]?.childNodes?.get(0)?.childNodes?.asList()?.firstNotNullOf { n -> (n as? Element)?.getAttribute("href") }
            val split = link?.split("-") ?: return@mapNotNull null

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
