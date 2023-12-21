package tk.zwander.common.util

import korlibs.io.serialization.xml.Xml
import korlibs.io.serialization.xml.XmlBuilder

fun XmlBuilder.textNode(tag: String, text: String): Xml {
    return node(tag) {
        text(text)
    }
}

fun XmlBuilder.dataNode(tag: String, text: String): Xml {
    return node(tag) {
        textNode("Data", text)
    }
}
