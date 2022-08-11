package org.jsoup.nodes

import jsoup.UncheckedIOException
import java.io.IOException

/**
 * A Character Data node, to support CDATA sections.
 */
class CDataNode(text: String?) : TextNode(text!!) {
    override fun nodeName(): String? {
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
    public override fun outerHtmlHead(accum: Appendable?, depth: Int, out: Document.OutputSettings?) {
        accum?.append("<![CDATA[")
            ?.append(wholeText)
    }

    public override fun outerHtmlTail(accum: Appendable?, depth: Int, out: Document.OutputSettings?) {
        try {
            accum!!.append("]]>")
        } catch (e: IOException) {
            throw jsoup.UncheckedIOException(e)
        }
    }

    override fun clone(): CDataNode {
        return super.clone() as CDataNode
    }
}
