package tk.zwander.common.util

import com.soywiz.korio.serialization.xml.Xml
import platform.WebKit.*
import tk.zwander.common.data.changelog.Changelog
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual object PlatformChangelogHandler {
    actual suspend fun parseDocUrl(body: String): String? {
        val wv = WKWebView()

        val doc = suspendCoroutine { continuation ->
            wv.evaluateJavaScript(
                "val match = document.querySelector('#sel_lang_hidden')"
            ) { result, error ->
                continuation.resume(result)
            }
        }

        println("doc $doc")

        val xml = Xml.parse(body)
        val selector = xml.descendants.find { it.attribute("id") == "sel_lang_hidden" }
        val engOption = selector?.allChildren?.run {
            find { it.attribute("value") == "EN" } ?: first()
        }

        return engOption?.text.also {
            println("Found option $it")
        }
    }

    actual suspend fun parseChangelogs(body: String): Map<String, Changelog> {
//        val doc = DOMHTMLDocument.alloc()!!
//        doc.write(body)
//
//        val container = doc.querySelector(".container")
//        val divs = container!!.childNodes?.toList()?.toMutableList()?.apply {
//            removeAll { it.nodeName == "hr" }
//        }!!
//        val changelogs = LinkedHashMap<String, Changelog>()
//
//        for (i in 3 until divs.size step 2) {
//            val row = divs[i].childNodes!!.toList()
//            val log = divs[i + 1]
//
//            val (build, androidVer, relDate, secPatch, _) = when (row.size) {
//                4 -> {
//                    Changelog(
//                        row[0].textContent()!!.split(":")[1].trim(),
//                        row[1].textContent()!!.split(":")[1].trim(),
//                        row[2].textContent()!!.split(":")[1].trim(),
//                        row[3].textContent()!!.split(":")[1].trim(),
//                        null
//                    )
//                }
//                3 -> {
//                    Changelog(
//                        row[0].textContent()!!.split(":")[1].trim(),
//                        row[1].textContent()!!.split(":")[1].trim(),
//                        row[2].textContent()!!.split(":")[1].trim(),
//                        null, null
//                    )
//                }
//                else -> {
//                    Changelog(null, null, null, null, null)
//                }
//            }
//
//            val logText = log.childNodes!!.toList()[0].childNodes!!.toList().joinToString(separator = "") { it.toString() }
//
//            if (build != null) {
//                changelogs[build] = Changelog(
//                    build, androidVer, relDate, secPatch, logText
//                )
//            }
//        }
//
//        return changelogs

        return LinkedHashMap()
    }
}
