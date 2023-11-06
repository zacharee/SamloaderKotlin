package tk.zwander.commonCompose.view.pages

import com.soywiz.klock.DateFormat
import com.soywiz.korio.serialization.xml.Xml
import com.soywiz.korio.serialization.xml.allChildren
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.util.makeFirmwareString

actual object PlatformHistoryView {
    actual suspend fun parseHistory(body: String): List<HistoryInfo> {
        val doc = Xml.parse(body)

        val listItems = doc.descendants.filter { it.attribute("class") == "index_list" }.toMutableList().apply {
            removeAt(0)
        }

        return listItems.map {
            val cols = it.descendants.filter { it.attribute("class") == "index_body_list" }
            val children = cols.allChildren.toList()
            val date = children[6].text
            val version = children[5].text

            val link = children[0].allChildren.first().allChildren.first { it.hasAttribute("href") }.attribute("href")
            val split = link!!.split("-")

            val pda = split[split.lastIndex - 1]
            val csc = split[split.lastIndex].split(".")[0]

            val formats = arrayOf(
                "yyyy/M/d",
                "yyyy-M-d",
                "M/d/yyyy"
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
