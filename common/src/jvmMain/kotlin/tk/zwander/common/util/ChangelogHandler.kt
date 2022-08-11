package tk.zwander.common.util

import io.ktor.util.*
import jsoup.Jsoup
import tk.zwander.common.data.changelog.Changelog

actual object PlatformChangelogHandler {
    actual suspend fun parseDocUrl(body: String): String? {
        val doc = jsoup.Jsoup.parse(body)
        val selector = doc.selectFirst("#sel_lang_hidden")
        val engOption = selector?.children()?.run { find { it.attr("value") == "EN" } ?: first() }

        return engOption?.text()
    }

    @OptIn(InternalAPI::class)
    actual suspend fun parseChangelogs(body: String): Map<String, Changelog> {
        val doc = jsoup.Jsoup.parse(body)
        val container = doc.selectFirst(".container")

        val divs = container!!.children().apply {
            removeIf { it.tagName() == "hr" }
        }
        val changelogs = LinkedHashMap<String, Changelog>()

        for (i in 3 until divs.size step 2) {
            val row = divs[i].children()
            val log = divs[i + 1]

            //This is kind of messy, but Samsung doesn't have a proper API for retrieving
            //version info. Some firmware entries don't have a security patch field, so
            //this handles that case. Some entries are in other languages, so using text
            //searching doesn't work well. It's possible some entries are missing other
            //fields, but there aren't any examples of that yet.
            val (build, androidVer, relDate, secPatch, _) = when {
                row.count() == 4 -> {
                    Changelog(
                        row[0].text().split(":")[1].trim(),
                        row[1].text().split(":")[1].trim(),
                        row[2].text().split(":")[1].trim(),
                        row[3].text().split(":")[1].trim(),
                        null
                    )
                }
                row.count() == 3 -> {
                    Changelog(
                        row[0].text().split(":")[1].trim(),
                        row[1].text().split(":")[1].trim(),
                        row[2].text().split(":")[1].trim(),
                        null, null
                    )
                }
                else -> {
                    Changelog(null, null, null, null, null)
                }
            }

            val logText = log.children()[0].childNodes().joinToString(separator = "", transform = { it.outerHtml() })

            if (build != null) {
                changelogs[build] = Changelog(
                    build, androidVer, relDate, secPatch, logText
                )
            }
        }

        return changelogs
    }
}
