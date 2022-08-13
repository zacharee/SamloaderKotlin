package org.jsoup.nodes

import io.ktor.utils.io.errors.*
import org.jsoup.UncheckedIOException

/**
 * A Character Data node, to support CDATA sections.
 */
class CDataNode(text: String?) : TextNode(text!!) {
    override fun newInstance(): Node {
        return CDataNode(text())
    }

    override fun nodeName(): String {
        return "#cdata"
    }

    /**
     * Get the unencoded, **non-normalized** text content of this CDataNode.
     * @return unencoded, non-normalized text
     */
    override fun text(): String? {
        return wholeText
    }

    @Throws(IOException::class)
    override fun outerHtmlHead(accum: Appendable, depth: Int, out: Document.OutputSettings) {
        accum.append("<![CDATA[")
            .append(wholeText)
    }

    override fun outerHtmlTail(accum: Appendable, depth: Int, out: Document.OutputSettings) {
        try {
            accum.append("]]>")
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    override fun clone(): CDataNode {
        return super.clone() as CDataNode
    }
}
