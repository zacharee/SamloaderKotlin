package org.jsoup.parser

import org.jsoup.helper.Validate
import org.jsoup.nodes.*

/**
 * Use the `XmlTreeBuilder` when you want to parse XML without any of the HTML DOM rules being applied to the
 * document.
 *
 * Usage example: `Document xmlDoc = Jsoup.parse(html, baseUrl, Parser.xmlParser());`
 *
 * @author Jonathan Hedley
 */
class XmlTreeBuilder constructor() : TreeBuilder() {
    public override fun defaultSettings(): ParseSettings? {
        return ParseSettings.Companion.preserveCase
    }

    override fun initialiseParse(input: String, baseUri: String?, parser: Parser) {
        super.initialiseParse(input, baseUri, parser)
        stack.add((doc)!!) // place the document onto the stack. differs from HtmlTreeBuilder (not on stack)
        doc!!.outputSettings()
            ?.syntax(Document.OutputSettings.Syntax.xml)
            ?.escapeMode(Entities.EscapeMode.xhtml)
            ?.prettyPrint(false) // as XML, we don't understand what whitespace is significant or not
    }

    fun parse(input: String, baseUri: String?): Document? {
        return parse((input), baseUri, Parser(this))
    }

    public override fun newInstance(): XmlTreeBuilder {
        return XmlTreeBuilder()
    }

    override fun process(token: Token): Boolean {
        // start tag, end tag, doctype, comment, character, eof
        when (token.type) {
            Token.TokenType.StartTag -> insert(token.asStartTag())
            Token.TokenType.EndTag -> popStackToClose(token.asEndTag())
            Token.TokenType.Comment -> insert(token.asComment())
            Token.TokenType.Character -> insert(token.asCharacter())
            Token.TokenType.Doctype -> insert(token.asDoctype())
            Token.TokenType.EOF -> {}
            else -> Validate.fail("Unexpected token type: " + token.type)
        }
        return true
    }

    protected fun insertNode(node: Node) {
        currentElement().appendChild(node)
        onNodeInserted((node), null)
    }

    protected fun insertNode(node: Node, token: Token?) {
        currentElement().appendChild(node)
        onNodeInserted((node), token)
    }

    fun insert(startTag: Token.StartTag?): Element {
        val tag: Tag? = tagFor((startTag!!.name())!!, settings)
        // todo: wonder if for xml parsing, should treat all tags as unknown? because it's not html.
        if (startTag.hasAttributes()) startTag.attributes!!.deduplicate((settings)!!)
        val el: Element = Element(tag, null, settings!!.normalizeAttributes(startTag.attributes))
        insertNode(el, startTag)
        if (startTag.isSelfClosing) {
            if (!tag!!.isKnownTag) // unknown tag, remember this is self closing for output. see above.
                tag.setSelfClosing()
        } else {
            stack.add(el)
        }
        return el
    }

    fun insert(commentToken: Token.Comment) {
        val comment: Comment = Comment(commentToken!!.getData())
        var insert: Node = comment
        if (commentToken.bogus && comment.isXmlDeclaration) {
            // xml declarations are emitted as bogus comments (which is right for html, but not xml)
            // so we do a bit of a hack and parse the data as an element to pull the attributes out
            val decl: XmlDeclaration? =
                comment.asXmlDeclaration() // else, we couldn't parse it as a decl, so leave as a comment
            if (decl != null) insert = decl
        }
        insertNode(insert, commentToken)
    }

    fun insert(token: Token.Character) {
        val data: String? = token.data
        insertNode(if (token.isCData) CDataNode(data) else TextNode((data)!!), token)
    }

    fun insert(d: Token.Doctype?) {
        val doctypeNode: DocumentType =
            DocumentType(settings!!.normalizeTag(d!!.getName()), d.getPublicIdentifier(), d.getSystemIdentifier())
        doctypeNode.setPubSysKey(d.pubSysKey)
        insertNode(doctypeNode, d)
    }

    /**
     * If the stack contains an element with this tag's name, pop up the stack to remove the first occurrence. If not
     * found, skips.
     *
     * @param endTag tag to close
     */
    protected fun popStackToClose(endTag: Token.EndTag?) {
        // like in HtmlTreeBuilder - don't scan up forever for very (artificially) deeply nested stacks
        val elName: String? = settings!!.normalizeTag(endTag!!.tagName)
        var firstFound: Element? = null
        val bottom: Int = stack!!.size - 1
        val upper: Int = if (bottom >= maxQueueDepth) bottom - maxQueueDepth else 0
        for (pos in stack!!.size - 1 downTo upper) {
            val next: Element = stack!!.get(pos)
            if ((next.nodeName() == elName)) {
                firstFound = next
                break
            }
        }
        if (firstFound == null) return  // not found, skip
        for (pos in stack!!.indices.reversed()) {
            val next: Element = stack!!.get(pos)
            stack!!.removeAt(pos)
            if (next === firstFound) {
                onNodeClosed(next, endTag)
                break
            }
        }
    }

    fun parseFragment(inputFragment: String, baseUri: String?, parser: Parser): List<Node> {
        initialiseParse((inputFragment), baseUri, parser)
        runParser()
        return doc!!.childNodes()
    }

    public override fun parseFragment(
        inputFragment: String?,
        context: Element?,
        baseUri: String?,
        parser: Parser
    ): List<Node> {
        return parseFragment(inputFragment ?: "", baseUri, parser)
    }

    companion object {
        private val maxQueueDepth: Int = 256 // an arbitrary tension point between real XML and crafted pain
    }
}
