package tk.zwander.common.util

import io.ktor.util.*
import org.jsoup.Jsoup
import tk.zwander.common.data.changelog.Changelog

actual object PlatformChangelogHandler {
    actual suspend fun parseDocUrl(body: String): String {
        val doc = Jsoup.parse(body)
        val selector = doc.selectFirst("#sel_lang_hidden")
        val engOption = selector.children().find { it.attr("value") == "EN" }!!

        return engOption.text()
    }

    @OptIn(InternalAPI::class)
    actual suspend fun parseChangelogs(body: String): Map<String, Changelog> {
        val doc = Jsoup.parse(body)
        val container = doc.selectFirst(".container")

        val divs = container.children().apply {
            removeIf { it.tagName() == "hr" }
        }
        val changelogs = LinkedHashMap<String, Changelog>()

        for (i in 3 until divs.size step 2) {
            val row = divs[i].children()
            val log = divs[i + 1]

            val build = row[0].text().split(":")[1].trim()
            val androidVer = row[1].text().split(":")[1].trim()
            val relDate = row[2].text().split(":")[1].trim()
            val secPatch = row[3].text().split(":")[1].trim()

            val logText = log.children()[0].childNodes().joinToString(separator = "", transform = { it.outerHtml() })

            changelogs[build] = Changelog(
                build, androidVer, relDate, secPatch, logText
            )
        }

        return changelogs
    }
}