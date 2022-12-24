package tk.zwander.common.util

//import cocoapods.HTMLReader.HTMLDocument
//import cocoapods.HTMLReader.HTMLElement
//import cocoapods.HTMLReader.HTMLNode
//import cocoapods.HTMLReader.firstNodeMatchingSelector
import kotlinx.cinterop.convert
import platform.Foundation.NSCoder
import platform.Foundation.NSData
import platform.Foundation.NSOrderedSet
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.WebKit.*
import tk.zwander.common.data.changelog.Changelog
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private fun DOMNodeList.toList(): List<DOMNode> {
    return arrayListOf<DOMNode>().apply {
        for (i in 0u until this@toList.length) {
            item(i)?.let {
                add(it)
            }
        }
    }
}

//private fun NSOrderedSet.toList(): List<HTMLNode> {
//    return arrayListOf<HTMLNode>().apply {
//        for (i in 0 until count.toInt()) {
//            (this@toList.objectAtIndex(i.convert()) as? HTMLNode)?.let {
//                add(it)
//            }
//        }
//    }
//}

actual object PlatformChangelogHandler {
    actual suspend fun parseDocUrl(body: String): String? {
//        val doc = DOMHTMLDocument.alloc()!!
//        doc.write(body)

        val wv = WKWebView()
//        wv.loadHTMLString(body, null)

        val doc = suspendCoroutine { continuation ->
            wv.evaluateJavaScript(
                "val match = document.querySelector('#sel_lang_hidden')"
            ) { result, error ->
                continuation.resume(result)
            }
        }

        println("doc $doc")

//        val selector = doc.querySelector("#sel_lang_hidden")
//
//        val engOption = selector?.childNodes?.toList()?.run {
//            find { it.attributes?.getNamedItem("value")?.toString() == "EN" } ?: first()
//        }
//
//        return engOption?.textContent
        return null
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
