package tk.zwander.common.util

import com.soywiz.korio.serialization.xml.Xml
import io.ktor.util.*
import tk.zwander.common.data.changelog.Changelog

actual object PlatformChangelogHandler {
    actual suspend fun parseDocUrl(body: String): String? {
        val doc = Xml(body)
        val selector = doc.descendants.find { it.attribute("id") == "sel_lang_hidden" }
        val engOption = selector?.allChildren?.run { find { it.attribute("value") == "EN" } ?: first() }

        return engOption?.text
    }

    @OptIn(InternalAPI::class)
    actual suspend fun parseChangelogs(body: String): Map<String, Changelog> {
        val doc = Xml(body)
        val container = doc.descendants.first { it.attribute("class") == "container" }

        val divs = container.allChildren.toMutableList().apply {
            removeAll { it.name == "hr" }
        }
        val changelogs = LinkedHashMap<String, Changelog>()

        for (i in 3 until divs.size step 2) {
            val row = divs[i].allChildren
            val log = divs[i + 1]

            //This is kind of messy, but Samsung doesn't have a proper API for retrieving
            //version info. Some firmware entries don't have a security patch field, so
            //this handles that case. Some entries are in other languages, so using text
            //searching doesn't work well. It's possible some entries are missing other
            //fields, but there aren't any examples of that yet.
            val (build, androidVer, relDate, secPatch, _) = when {
                row.count() == 4 -> {
                    Changelog(
                        row[0].text.split(":")[1].trim(),
                        row[1].text.split(":")[1].trim(),
                        row[2].text.split(":")[1].trim(),
                        row[3].text.split(":")[1].trim(),
                        null
                    )
                }
                row.count() == 3 -> {
                    Changelog(
                        row[0].text.split(":")[1].trim(),
                        row[1].text.split(":")[1].trim(),
                        row[2].text.split(":")[1].trim(),
                        null, null
                    )
                }
                else -> {
                    Changelog(null, null, null, null, null)
                }
            }

            val logText = log.allChildren[0].allChildren.joinToString(separator = "", transform = { it.outerXml })

            if (build != null) {
                changelogs[build] = Changelog(
                    build, androidVer, relDate, secPatch, logText
                )
            }
        }

        return changelogs
    }
}
