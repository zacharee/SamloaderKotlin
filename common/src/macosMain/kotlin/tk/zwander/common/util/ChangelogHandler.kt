package tk.zwander.common.util

import cocoapods.HTMLReader.HTMLDocument
import cocoapods.HTMLReader.HTMLElement
import cocoapods.HTMLReader.HTMLNode
import cocoapods.HTMLReader.firstNodeMatchingSelector
import kotlinx.cinterop.convert
import platform.Foundation.NSOrderedSet
import platform.WebKit.*
import tk.zwander.common.data.changelog.Changelog

private fun DOMNodeList.toList(): List<DOMNode> {
    return arrayListOf<DOMNode>().apply {
        for (i in 0u until this@toList.length) {
            item(i)?.let {
                add(it)
            }
        }
    }
}

private fun NSOrderedSet.toList(): List<HTMLNode> {
    return arrayListOf<HTMLNode>().apply {
        for (i in 0 until count.toInt()) {
            (this@toList.objectAtIndex(i.convert()) as? HTMLNode)?.let {
                add(it)
            }
        }
    }
}

actual object PlatformChangelogHandler {
    actual suspend fun parseDocUrl(body: String): String? {
        val doc = HTMLDocument(body)

        val selector = doc.firstNodeMatchingSelector("#sel_lang_hidden")
        val engOption = selector?.children?.toList()?.run {
            find { (it as? HTMLElement)?.attributes?.get("value") == "EN" } ?: first()
        }

        return engOption?.textContent
    }

    actual suspend fun parseChangelogs(body: String): Map<String, Changelog> {
        val doc = HTMLDocument(body)

        val container = doc.firstNodeMatchingSelector(".container")
        val divs = container!!.children.toList().toMutableList().apply {
            removeAll { (it as? HTMLElement)?.tagName == "hr" }
        }
        val changelogs = LinkedHashMap<String, Changelog>()

        for (i in 3 until divs.size step 2) {
            val row = divs[i].children.toList()
            val log = divs[i + 1]

            val (build, androidVer, relDate, secPatch, _) = when (row.size) {
                4 -> {
                    Changelog(
                        row[0].textContent().split(":")[1].trim(),
                        row[1].textContent().split(":")[1].trim(),
                        row[2].textContent().split(":")[1].trim(),
                        row[3].textContent().split(":")[1].trim(),
                        null
                    )
                }
                3 -> {
                    Changelog(
                        row[0].textContent().split(":")[1].trim(),
                        row[1].textContent().split(":")[1].trim(),
                        row[2].textContent().split(":")[1].trim(),
                        null, null
                    )
                }
                else -> {
                    Changelog(null, null, null, null, null)
                }
            }

            val logText = log.children.toList()[0].children.toList().joinToString(separator = "") { (it as? HTMLElement)?.toString() ?: "" }

            if (build != null) {
                changelogs[build] = Changelog(
                    build, androidVer, relDate, secPatch, logText
                )
            }
        }

        return changelogs
    }
}
