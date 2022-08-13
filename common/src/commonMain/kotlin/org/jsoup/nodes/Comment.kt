package org.jsoup.nodes

import io.ktor.utils.io.errors.*
import org.jsoup.parser.ParseSettings
import org.jsoup.parser.Parser

/**
 * A comment node.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
class Comment(data: String) : LeafNode() {
    /**
     * Create a new comment node.
     * @param data The contents of the comment
     */
    init {
        value = data
    }

    override fun newInstance(): Node {
        return Comment(value.toString())
    }

    override fun nodeName(): String {
        return "#comment"
    }

    /**
     * Get the contents of the comment.
     * @return comment content
     */
    val data: String
        get() = coreValue()

    fun setData(data: String): Comment {
        coreValue(data)
        return this
    }

    @Throws(IOException::class)
    override fun outerHtmlHead(accum: Appendable, depth: Int, out: Document.OutputSettings) {
        if (out.prettyPrint() && (siblingIndex() == 0 && parNode is Element && (parNode as Element).tag()
                .formatAsBlock() || out.outline())
        ) {
            indent(accum, depth, out)
        }
        accum.append("<!--")
            .append(data)
            .append("-->")
    }

    override fun outerHtmlTail(accum: Appendable, depth: Int, out: Document.OutputSettings) {}

    override fun toString(): String {
        return outerHtml()!!
    }

    override fun clone(): Comment {
        return super.clone() as Comment
    }

    /**
     * Check if this comment looks like an XML Declaration.
     * @return true if it looks like, maybe, it's an XML Declaration.
     */
    val isXmlDeclaration: Boolean
        get() {
            val data = data
            return isXmlDeclarationData(data)
        }

    /**
     * Attempt to cast this comment to an XML Declaration node.
     * @return an XML declaration if it could be parsed as one, null otherwise.
     */
    fun asXmlDeclaration(): XmlDeclaration? {
        val data = data
        var decl: XmlDeclaration? = null
        val declContent = data.substring(1, data.length - 1)
        // make sure this bogus comment is not immediately followed by another, treat as comment if so
        if (isXmlDeclarationData(declContent)) return null
        val fragment = "<$declContent>"
        // use the HTML parser not XML, so we don't get into a recursive XML Declaration on contrived data
        val doc: Document =
            Parser.htmlParser().settings(ParseSettings.preserveCase).parseInput(fragment, baseUri())
        if (doc.body().children().size > 0) {
            val el = doc.body().child(0)
            decl = XmlDeclaration(
                NodeUtils.parser(doc).settings().normalizeTag(el.tagName())!!,
                data.startsWith("!")
            )
            decl.attributes().addAll(el.attributes())
        }
        return decl
    }

    companion object {
        private fun isXmlDeclarationData(data: String?): Boolean {
            return data!!.length > 1 && (data.startsWith("!") || data.startsWith("?"))
        }
    }
}
