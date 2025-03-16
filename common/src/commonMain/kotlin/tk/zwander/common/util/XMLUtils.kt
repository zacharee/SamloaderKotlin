package tk.zwander.common.util

import org.redundent.kotlin.xml.Node

fun Node.textNode(tag: String, text: String): Node {
    return tag {
        -text
    }
}

fun Node.dataNode(tag: String, text: String): Node {
    return tag {
        textNode("Data", text)
    }
}
