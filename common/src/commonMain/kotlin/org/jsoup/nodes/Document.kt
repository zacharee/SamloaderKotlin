package org.jsoup.nodes

import io.ktor.utils.io.charsets.*
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.helper.DataUtil
import org.jsoup.helper.Validate
import org.jsoup.internal.StringUtil
import org.jsoup.parser.ParseSettings
import org.jsoup.parser.Parser
import org.jsoup.parser.Tag
import org.jsoup.select.Evaluator

/**
 * A HTML Document.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
class Document(private val location: String?) :
    Element(Tag.Companion.valueOf("#root", ParseSettings.htmlDefault), location) {
    private var connection // the connection this doc was fetched from, if any
            : Connection? = null
    private var outputSettings: OutputSettings? = OutputSettings()
    private var parser // the parser used to parse this document
            : Parser?
    private var quirksMode: QuirksMode? = QuirksMode.noQuirks
    private var updateMetaCharset = false

    /**
     * Get the URL this Document was parsed from. If the starting URL is a redirect,
     * this will return the final URL from which the document was served from.
     *
     * Will return an empty string if the location is unknown (e.g. if parsed from a String).
     * @return location
     */
    fun location(): String? {
        return location
    }

//    /**
//     * Returns the Connection (Request/Response) object that was used to fetch this document, if any; otherwise, a new
//     * default Connection object. This can be used to continue a session, preserving settings and cookies, etc.
//     * @return the Connection (session) associated with this Document, or an empty one otherwise.
//     * @see Connection.newRequest
//     */
//    fun connection(): Connection? {
//        return if (connection == null) Jsoup.newSession() else connection
//    }

    /**
     * Returns this Document's doctype.
     * @return document type, or null if not set
     */

    fun documentType(): DocumentType? {
        for (node in childElements) {
            if (node is DocumentType) return node else if (node !is LeafNode) // scans forward across comments, text, processing instructions etc
                break
        }
        return null
        // todo - add a set document type?
    }

    /**
     * Find the root HTML element, or create it if it doesn't exist.
     * @return the root HTML element.
     */
    private fun htmlEl(): Element {
        for (el in childElementsList()) {
            if (el.normalName() == "html") return el
        }
        return appendElement("html")
    }

    /**
     * Get this document's `head` element.
     *
     *
     * As a side-effect, if this Document does not already have a HTML structure, it will be created. If you do not want
     * that, use `#selectFirst("head")` instead.
     *
     * @return `head` element.
     */
    fun head(): Element {
        val html = htmlEl()
        for (el in html.childElementsList()) {
            if (el.normalName() == "head") return el
        }
        return html.prependElement("head")
    }

    /**
     * Get this document's `<body>` or `<frameset>` element.
     *
     *
     * As a **side-effect**, if this Document does not already have a HTML structure, it will be created with a `<body>` element. If you do not want that, use `#selectFirst("body")` instead.
     *
     * @return `body` element for documents with a `<body>`, a new `<body>` element if the document
     * had no contents, or the outermost `<frameset> element` for frameset documents.
     */
    fun body(): Element {
        val html = htmlEl()
        for (el in html.childElementsList()) {
            if ("body" == el.normalName() || "frameset" == el.normalName()) return el
        }
        return html.appendElement("body")
    }

    /**
     * Get the string contents of the document's `title` element.
     * @return Trimmed title, or empty string if none set.
     */
    fun title(): String {
        // title is a preserve whitespace tag (for document output), but normalised here
        val titleEl = head().selectFirst(titleEval)
        return if (titleEl != null) StringUtil.normaliseWhitespace(titleEl.text()).trim { it <= ' ' } else ""
    }

    /**
     * Create a new, empty Document.
     * @param baseUri base URI of document
     * @see Jsoup.parse
     * @see .createShell
     */
    init {
        parser = Parser.htmlParser() // default, but overridable
    }

    /**
     * Set the document's `title` element. Updates the existing element, or adds `title` to `head` if
     * not present
     * @param title string to set as title
     */
    fun title(title: String) {
        Validate.notNull(title)
        var titleEl = head().selectFirst(titleEval)
        if (titleEl == null) // add to head
            titleEl = head().appendElement("title")
        titleEl.text(title)
    }

    /**
     * Create a new Element, with this document's base uri. Does not make the new element a child of this document.
     * @param tagName element tag name (e.g. `a`)
     * @return new element
     */
    fun createElement(tagName: String?): Element {
        return Element(Tag.valueOf(tagName, ParseSettings.preserveCase), baseUri())
    }

    /**
     * Normalise the document. This happens after the parse phase so generally does not need to be called.
     * Moves any text content that is not in the body element into the body.
     * @return this document after normalisation
     */
    fun normalise(): Document {
        val htmlEl = htmlEl() // these all create if not found
        val head = head()
        body()

        // pull text nodes out of root, html, and head els, and push into body. non-text nodes are already taken care
        // of. do in inverse order to maintain text order.
        normaliseTextNodes(head)
        normaliseTextNodes(htmlEl)
        normaliseTextNodes(this)
        normaliseStructure("head", htmlEl)
        normaliseStructure("body", htmlEl)
        ensureMetaCharsetElement()
        return this
    }

    // does not recurse.
    private fun normaliseTextNodes(element: Element?) {
        val toMove: MutableList<Node> = ArrayList()
        for (node in element!!.childElements) {
            if (node is TextNode) {
                if (!node.isBlank) toMove.add(node)
            }
        }
        for (i in toMove.indices.reversed()) {
            val node = toMove[i]
            element.removeChild(node)
            body().prependChild(TextNode(" "))
            body().prependChild(node)
        }
    }

    // merge multiple <head> or <body> contents into one, delete the remainder, and ensure they are owned by <html>
    private fun normaliseStructure(tag: String, htmlEl: Element?) {
        val elements = getElementsByTag(tag)
        val master = elements!!.first() // will always be available as created above if not existent
        if (elements.size > 1) { // dupes, move contents to master
            val toMove: MutableList<Node?> = ArrayList()
            for (i in 1 until elements.size) {
                val dupe: Node = elements[i]
                toMove.addAll(dupe.ensureChildNodes())
                dupe.remove()
            }
            for (dupe in toMove) master!!.appendChild(dupe!!)
        }
        // ensure parented by <html>
        if (master!!.parent() != null && master.parent() != htmlEl) {
            htmlEl!!.appendChild(master) // includes remove()
        }
    }

    override fun outerHtml(): String {
        return super.html() // no outer wrapper tag
    }

    /**
     * Set the text of the `body` of this document. Any existing nodes within the body will be cleared.
     * @param text unencoded text
     * @return this document
     */
    override fun text(text: String): Element {
        body().text(text) // overridden to not nuke doc structure
        return this
    }

    override fun nodeName(): String? {
        return "#document"
    }

    /**
     * Sets the charset used in this document. This method is equivalent
     * to [ OutputSettings.charset(Charset)][OutputSettings.charset] but in addition it updates the
     * charset / encoding element within the document.
     *
     *
     * This enables
     * [meta charset update][.updateMetaCharsetElement].
     *
     *
     * If there's no element with charset / encoding information yet it will
     * be created. Obsolete charset / encoding definitions are removed!
     *
     *
     * **Elements used:**
     *
     *
     *  * **Html:** *&lt;meta charset="CHARSET"&gt;*
     *  * **Xml:** *&lt;?xml version="1.0" encoding="CHARSET"&gt;*
     *
     *
     * @param charset Charset
     *
     * @see .updateMetaCharsetElement
     * @see OutputSettings.charset
     */
    fun charset(charset: Charset) {
        updateMetaCharsetElement(true)
        outputSettings!!.charset(charset)
        ensureMetaCharsetElement()
    }

    /**
     * Returns the charset used in this document. This method is equivalent
     * to [OutputSettings.charset].
     *
     * @return Current Charset
     *
     * @see OutputSettings.charset
     */
    fun charset(): Charset {
        return outputSettings!!.charset()
    }

    /**
     * Sets whether the element with charset information in this document is
     * updated on changes through [ Document.charset(Charset)][.charset] or not.
     *
     *
     * If set to <tt>false</tt> *(default)* there are no elements
     * modified.
     *
     * @param update If <tt>true</tt> the element updated on charset
     * changes, <tt>false</tt> if not
     *
     * @see .charset
     */
    fun updateMetaCharsetElement(update: Boolean) {
        updateMetaCharset = update
    }

    /**
     * Returns whether the element with charset information in this document is
     * updated on changes through [ Document.charset(Charset)][.charset] or not.
     *
     * @return Returns <tt>true</tt> if the element is updated on charset
     * changes, <tt>false</tt> if not
     */
    fun updateMetaCharsetElement(): Boolean {
        return updateMetaCharset
    }

    override fun clone(): Document {
        val clone = super.clone() as Document
        clone.outputSettings = outputSettings!!.clone()
        return clone
    }

    override fun shallowClone(): Document {
        val clone = Document(baseUri())
        if (attrs != null) clone.attrs = attrs!!.clone()
        clone.outputSettings = outputSettings!!.clone()
        return clone
    }

    /**
     * Ensures a meta charset (html) or xml declaration (xml) with the current
     * encoding used. This only applies with
     * [updateMetaCharset][.updateMetaCharsetElement] set to
     * <tt>true</tt>, otherwise this method does nothing.
     *
     *
     *  * An existing element gets updated with the current charset
     *  * If there's no element yet it will be inserted
     *  * Obsolete elements are removed
     *
     *
     *
     * **Elements used:**
     *
     *
     *  * **Html:** *&lt;meta charset="CHARSET"&gt;*
     *  * **Xml:** *&lt;?xml version="1.0" encoding="CHARSET"&gt;*
     *
     */
    private fun ensureMetaCharsetElement() {
        if (updateMetaCharset) {
            val syntax = outputSettings()!!.syntax()
            if (syntax == OutputSettings.Syntax.html) {
                val metaCharset = selectFirst("meta[charset]")
                if (metaCharset != null) {
                    metaCharset.attr("charset", charset().name)
                } else {
                    head().appendElement("meta").attr("charset", charset().name)
                }
                select("meta[name=charset]")!!.remove() // Remove obsolete elements
            } else if (syntax == OutputSettings.Syntax.xml) {
                val node = ensureChildNodes()[0]
                if (node is XmlDeclaration) {
                    var decl = node
                    if (decl.name() == "xml") {
                        decl.attr("encoding", charset().name)
                        if (decl.hasAttr("version")) decl.attr("version", "1.0")
                    } else {
                        decl = XmlDeclaration("xml", false)
                        decl.attr("version", "1.0")
                        decl.attr("encoding", charset().name)
                        prependChild(decl)
                    }
                } else {
                    val decl = XmlDeclaration("xml", false)
                    decl.attr("version", "1.0")
                    decl.attr("encoding", charset().name)
                    prependChild(decl)
                }
            }
        }
    }

    /**
     * A Document's output settings control the form of the text() and html() methods.
     */
    data class OutputSettings(
        private var escapeMode: Entities.EscapeMode = Entities.EscapeMode.base(),
        private var charset: Charset = DataUtil.UTF_8,
        private var encoderThreadLocal: CharsetEncoder = charset.newEncoder(), // initialized by start of OuterHtmlVisitor
        var coreCharset: Entities.CoreCharset? = null,
        private var prettyPrint: Boolean = true,
        private var outline: Boolean = false,
        private var indentAmount: Int = 1,
        private var maxPaddingWidth: Int = 30,
        private var syntax: Syntax = Syntax.html,
    ) {
        /**
         * The output serialization syntax.
         */
        enum class Syntax {
            html, xml
        }

        /**
         * Get the document's current HTML escape mode: `base`, which provides a limited set of named HTML
         * entities and escapes other characters as numbered entities for maximum compatibility; or `extended`,
         * which uses the complete set of HTML named entities.
         *
         *
         * The default escape mode is `base`.
         * @return the document's current escape mode
         */
        fun escapeMode(): Entities.EscapeMode {
            return escapeMode
        }

        /**
         * Set the document's escape mode, which determines how characters are escaped when the output character set
         * does not support a given character:- using either a named or a numbered escape.
         * @param escapeMode the new escape mode to use
         * @return the document's output settings, for chaining
         */
        fun escapeMode(escapeMode: Entities.EscapeMode): OutputSettings {
            this.escapeMode = escapeMode
            return this
        }

        /**
         * Get the document's current output charset, which is used to control which characters are escaped when
         * generating HTML (via the `html()` methods), and which are kept intact.
         *
         *
         * Where possible (when parsing from a URL or File), the document's output charset is automatically set to the
         * input charset. Otherwise, it defaults to UTF-8.
         * @return the document's current charset.
         */
        fun charset(): Charset {
            return charset
        }

        /**
         * Update the document's output charset.
         * @param charset the new charset to use.
         * @return the document's output settings, for chaining
         */
        fun charset(charset: Charset): OutputSettings {
            this.charset = charset
            return this
        }

        /**
         * Update the document's output charset.
         * @param charset the new charset (by name) to use.
         * @return the document's output settings, for chaining
         */
        fun charset(charset: String): OutputSettings {
            charset(Charset.forName(charset))
            return this
        }

        fun prepareEncoder(): CharsetEncoder {
            // created at start of OuterHtmlVisitor so each pass has own encoder, so OutputSettings can be shared among threads
            val encoder = charset.newEncoder()
            encoderThreadLocal = (encoder)
            coreCharset = Entities.CoreCharset.byName(encoder.charset.name)
            return encoder
        }

        fun encoder(): CharsetEncoder {
            return encoderThreadLocal
        }

        /**
         * Get the document's current output syntax.
         * @return current syntax
         */
        fun syntax(): Syntax {
            return syntax
        }

        /**
         * Set the document's output syntax. Either `html`, with empty tags and boolean attributes (etc), or
         * `xml`, with self-closing tags.
         * @param syntax serialization syntax
         * @return the document's output settings, for chaining
         */
        fun syntax(syntax: Syntax): OutputSettings {
            this.syntax = syntax
            return this
        }

        /**
         * Get if pretty printing is enabled. Default is true. If disabled, the HTML output methods will not re-format
         * the output, and the output will generally look like the input.
         * @return if pretty printing is enabled.
         */
        fun prettyPrint(): Boolean {
            return prettyPrint
        }

        /**
         * Enable or disable pretty printing.
         * @param pretty new pretty print setting
         * @return this, for chaining
         */
        fun prettyPrint(pretty: Boolean): OutputSettings {
            prettyPrint = pretty
            return this
        }

        /**
         * Get if outline mode is enabled. Default is false. If enabled, the HTML output methods will consider
         * all tags as block.
         * @return if outline mode is enabled.
         */
        fun outline(): Boolean {
            return outline
        }

        /**
         * Enable or disable HTML outline mode.
         * @param outlineMode new outline setting
         * @return this, for chaining
         */
        fun outline(outlineMode: Boolean): OutputSettings {
            outline = outlineMode
            return this
        }

        /**
         * Get the current tag indent amount, used when pretty printing.
         * @return the current indent amount
         */
        fun indentAmount(): Int {
            return indentAmount
        }

        /**
         * Set the indent amount for pretty printing
         * @param indentAmount number of spaces to use for indenting each level. Must be &gt;= 0.
         * @return this, for chaining
         */
        fun indentAmount(indentAmount: Int): OutputSettings {
            Validate.isTrue(indentAmount >= 0)
            this.indentAmount = indentAmount
            return this
        }

        /**
         * Get the current max padding amount, used when pretty printing
         * so very deeply nested nodes don't get insane padding amounts.
         * @return the current indent amount
         */
        fun maxPaddingWidth(): Int {
            return maxPaddingWidth
        }

        /**
         * Set the max padding amount for pretty printing so very deeply nested nodes don't get insane padding amounts.
         * @param maxPaddingWidth number of spaces to use for indenting each level of nested nodes. Must be &gt;= -1.
         * Default is 30 and -1 means unlimited.
         * @return this, for chaining
         */
        fun maxPaddingWidth(maxPaddingWidth: Int): OutputSettings {
            Validate.isTrue(maxPaddingWidth >= -1)
            this.maxPaddingWidth = maxPaddingWidth
            return this
        }

        fun clone(): OutputSettings {
            val clone = copy()
            clone.charset(charset.name) // new charset and charset encoder
            clone.escapeMode = escapeMode.copy()
            // indentAmount, maxPaddingWidth, and prettyPrint are primitives so object.clone() will handle
            return clone
        }
    }

    /**
     * Get the document's current output settings.
     * @return the document's current output settings.
     */
    fun outputSettings(): OutputSettings? {
        return outputSettings
    }

    /**
     * Set the document's output settings.
     * @param outputSettings new output settings.
     * @return this document, for chaining.
     */
    fun outputSettings(outputSettings: OutputSettings?): Document {
        Validate.notNull(outputSettings)
        this.outputSettings = outputSettings
        return this
    }

    enum class QuirksMode {
        noQuirks, quirks, limitedQuirks
    }

    fun quirksMode(): QuirksMode? {
        return quirksMode
    }

    fun quirksMode(quirksMode: QuirksMode?): Document {
        this.quirksMode = quirksMode
        return this
    }

    /**
     * Get the parser that was used to parse this document.
     * @return the parser
     */
    fun parser(): Parser? {
        return parser
    }

    /**
     * Set the parser used to create this document. This parser is then used when further parsing within this document
     * is required.
     * @param parser the configured parser to use when further parsing is required for this document.
     * @return this document, for chaining.
     */
    fun parser(parser: Parser?): Document {
        this.parser = parser
        return this
    }

    /**
     * Set the Connection used to fetch this document. This Connection is used as a session object when further requests are
     * made (e.g. when a form is submitted).
     *
     * @param connection to set
     * @return this document, for chaining
     * @see Connection.newRequest
     * @since 1.14.1
     */
    fun connection(connection: Connection?): Document {
        Validate.notNull(connection)
        this.connection = connection
        return this
    }

    companion object {
        /**
         * Create a valid, empty shell of a document, suitable for adding more elements to.
         * @param baseUri baseUri of document
         * @return document with html, head, and body elements.
         */
        fun createShell(baseUri: String?): Document {
            Validate.notNull(baseUri)
            val doc = Document(baseUri)
            doc.parser = doc.parser()
            val html = doc.appendElement("html")
            html.appendElement("head")
            html.appendElement("body")
            return doc
        }

        private val titleEval: Evaluator = Evaluator.Tag("title")
    }
}
