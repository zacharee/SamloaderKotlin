package org.jsoup.nodes

import io.ktor.utils.io.errors.*
import org.jsoup.helper.Validate
import org.jsoup.internal.StringUtil

/**
 * A text node.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
open class TextNode(text: String) : LeafNode() {
    /**
     * Create a new TextNode representing the supplied (unencoded) text).
     *
     * @param text raw text
     * @see .createFromEncoded
     */
    init {
        value = text
    }

    override fun newInstance(): Node {
        return TextNode(value.toString())
    }

    override fun nodeName(): String {
        return "#text"
    }

    /**
     * Get the text content of this text node.
     * @return Unencoded, normalised text.
     * @see TextNode.getWholeText
     */
    open fun text(): String? {
        return StringUtil.normaliseWhitespace(wholeText)
    }

    /**
     * Set the text content of this text node.
     * @param text unencoded text
     * @return this, for chaining
     */
    fun text(text: String?): TextNode {
        coreValue(text)
        return this
    }

    /**
     * Get the (unencoded) text of this text node, including any newlines and spaces present in the original.
     * @return text
     */
    val wholeText: String?
        get() = coreValue()

    /**
     * Test if this text node is blank -- that is, empty or only whitespace (including newlines).
     * @return true if this document is empty or only whitespace, false if it contains any text content.
     */
    val isBlank: Boolean
        get() = StringUtil.isBlank(coreValue())

    /**
     * Split this text node into two nodes at the specified string offset. After splitting, this node will contain the
     * original text up to the offset, and will have a new text node sibling containing the text after the offset.
     * @param offset string offset point to split node at.
     * @return the newly created text node containing the text after the offset.
     */
    fun splitText(offset: Int): TextNode {
        val text = coreValue()
        Validate.isTrue(offset >= 0, "Split offset must be not be negative")
        Validate.isTrue(offset < text!!.length, "Split offset must not be greater than current text length")
        val head = text.substring(0, offset)
        val tail = text.substring(offset)
        text(head)
        val tailNode = TextNode(tail)
        if (parNode != null) parNode!!.addChildren(siblingIndex() + 1, tailNode)
        return tailNode
    }

    @Throws(IOException::class)
    override fun outerHtmlHead(accum: Appendable?, depth: Int, out: Document.OutputSettings?) {
        val prettyPrint = out!!.prettyPrint()
        val parent = if (parNode is Element) parNode as Element else null
        val normaliseWhite = prettyPrint && !Element.preserveWhitespace(parNode)
        var trimLeading = false
        var trimTrailing = false
        if (normaliseWhite) {
            trimLeading = sibIndex == 0 && parent != null && parent.tag().isBlock ||
                    parNode is Document
            trimTrailing = nextSibling() == null && parent != null && parent.tag().isBlock

            // if this text is just whitespace, and the next node will cause an indent, skip this text:
            val next = nextSibling()
            val couldSkip =
                next is Element && next.shouldIndent(out) || next is TextNode && next.isBlank // next is blank text, from re-parenting
            if (couldSkip && isBlank) return
            if (sibIndex == 0 && parent != null && parent.tag()
                    .formatAsBlock() && !isBlank || out.outline() && siblingNodes().isNotEmpty() && !isBlank
            ) indent(
                accum!!, depth, out
            )
        }
        Entities.escape(accum, coreValue(), out, false, normaliseWhite, trimLeading, trimTrailing)
    }

    override fun outerHtmlTail(accum: Appendable?, depth: Int, out: Document.OutputSettings?) {}

    override fun toString(): String {
        return outerHtml()!!
    }

    override fun clone(): TextNode {
        return super.clone() as TextNode
    }

    companion object {
        /**
         * Create a new TextNode from HTML encoded (aka escaped) data.
         * @param encodedText Text containing encoded HTML (e.g. &amp;lt;)
         * @return TextNode containing unencoded data (e.g. &lt;)
         */
        fun createFromEncoded(encodedText: String?): TextNode {
            val text = Entities.unescape(encodedText)
            return TextNode(text!!)
        }

        fun normaliseWhitespace(text: String?): String {
            var text = text
            text = StringUtil.normaliseWhitespace(text)
            return text
        }

        fun stripLeadingWhitespace(text: String): String {
            return text.replaceFirst("^\\s+".toRegex(), "")
        }

        fun lastCharIsWhitespace(sb: StringBuilder?): Boolean {
            return sb!!.isNotEmpty() && sb[sb.length - 1] == ' '
        }
    }
}
