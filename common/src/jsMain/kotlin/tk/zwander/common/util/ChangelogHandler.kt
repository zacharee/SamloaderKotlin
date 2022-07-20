package tk.zwander.common.util

import com.soywiz.korio.toList
import io.ktor.util.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLHRElement
import org.w3c.dom.get
import org.w3c.dom.parsing.DOMParser
import tk.zwander.common.data.changelog.Changelog

actual object PlatformChangelogHandler {
    actual suspend fun parseDocUrl(body: String): String? {
        val doc = DOMParser().parseFromString(body, "text/html")
        val selector = doc.getElementById("sel_lang_hidden")
        val engOption = selector?.children?.toList()?.run { find { it?.attributes?.get("value")?.value == "EN" } ?: first() }

        return engOption?.textContent
    }

    actual suspend fun parseChangelogs(body: String): Map<String, Changelog> {
        val doc = DOMParser().parseFromString(body, "text/html")
        val container = doc.getElementsByClassName("container")[0]

        val divs = container!!.children.toList().toMutableList().apply {
            removeAll { it is HTMLHRElement }
        }
        val changelogs = LinkedHashMap<String, Changelog>()

        for (i in 3 until divs.size step 2) {
            val row = divs[i]!!.children.toList()
            val log = divs[i + 1]

            //This is kind of messy, but Samsung doesn't have a proper API for retrieving
            //version info. Some firmware entries don't have a security patch field, so
            //this handles that case. Some entries are in other languages, so using text
            //searching doesn't work well. It's possible some entries are missing other
            //fields, but there aren't any examples of that yet.
            val (build, androidVer, relDate, secPatch, _) = when {
                row.count() == 4 -> {
                    Changelog(
                        row[0]!!.textContent!!.split(":")[1].trim(),
                        row[1]!!.textContent!!.split(":")[1].trim(),
                        row[2]!!.textContent!!.split(":")[1].trim(),
                        row[3]!!.textContent!!.split(":")[1].trim(),
                        null
                    )
                }
                row.count() == 3 -> {
                    Changelog(
                        row[0]!!.textContent!!.split(":")[1].trim(),
                        row[1]!!.textContent!!.split(":")[1].trim(),
                        row[2]!!.textContent!!.split(":")[1].trim(),
                        null, null
                    )
                }
                else -> {
                    Changelog(null, null, null, null, null)
                }
            }

            val logText = log!!.textContent

            if (build != null) {
                changelogs[build] = Changelog(
                    build, androidVer, relDate, secPatch, logText
                )
            }
        }

        return changelogs
    }
}
