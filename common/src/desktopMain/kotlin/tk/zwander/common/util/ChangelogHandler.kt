package tk.zwander.common.util

import com.soywiz.korio.lang.Charsets
import com.soywiz.korio.lang.toByteArray
import com.soywiz.korio.util.escape
import com.soywiz.korio.util.htmlspecialchars
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

            val build = row.find { it.text().contains("Build Number", true) }?.text()?.split(":")?.get(1)?.trim()
            val androidVer = row.find { it.text().contains("Android Version", true) }?.text()?.split(":")?.get(1)?.trim()
            val relDate = row.find { it.text().contains("Release Date", true) }?.text()?.split(":")?.get(1)?.trim()
            val secPatch = row.find { it.text().contains("Security Patch", true) }?.text()?.split(":")?.get(1)?.trim()

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