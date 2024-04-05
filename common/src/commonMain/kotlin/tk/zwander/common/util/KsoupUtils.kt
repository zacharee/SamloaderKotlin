package tk.zwander.common.util

import com.fleeksoft.ksoup.nodes.Element

fun Element.firstElementByTagName(name: String): Element? {
    return getElementsByTag(name).firstOrNull()
}
