package org.jsoup.parser

import org.jsoup.helper.Validate
import org.jsoup.nodes.*

/**
 * @author Jonathan Hedley
 */
abstract class TreeBuilder constructor() {
    var parser: Parser? = null
    var reader: CharacterReader? = null
    var tokeniser: Tokeniser? = null
    protected var doc // current doc we are building into
            : Document? = null
    open var stack // the stack of open elements
            : ArrayList<Element> = ArrayList(32)
    open var baseUri // current base uri, for creating new elements
            : String? = null
    protected var currentToken // currentToken is used only for error tracking.
            : Token? = null
    var settings: ParseSettings? = null
    protected var seenTags // tags we've used in this parse; saves tag GC for custom tags.
            : MutableMap<String, Tag?>? = null
    private val start: Token.StartTag = Token.StartTag() // start tag to process
    private val end: Token.EndTag = Token.EndTag()
    abstract fun defaultSettings(): ParseSettings?
    private var trackSourceRange // optionally tracks the source range of nodes
            : Boolean = false

    protected open fun initialiseParse(input: String, baseUri: String?, parser: Parser) {
        Validate.notNull(input, "String input must not be null")
        Validate.notNull(baseUri, "BaseURI must not be null")
        Validate.notNull(parser)
        doc = Document(baseUri)
        doc!!.parser(parser)
        this.parser = parser
        settings = parser.settings()
        reader = CharacterReader(input)
        trackSourceRange = parser.isTrackPosition
        reader!!.trackNewlines(parser.isTrackErrors || trackSourceRange) // when tracking errors or source ranges, enable newline tracking for better legibility
        currentToken = null
        tokeniser = Tokeniser(reader!!, parser.errors)
        seenTags = HashMap()
        this.baseUri = baseUri
    }

    fun parse(input: String, baseUri: String?, parser: Parser): Document? {
        initialiseParse(input, baseUri, parser)
        runParser()

        // tidy up - as the Parser and Treebuilder are retained in document for settings / fragments
        reader!!.close()
        reader = null
        tokeniser = null
        seenTags = null
        return doc
    }

    /**
     * Create a new copy of this TreeBuilder
     * @return copy, ready for a new parse
     */
    abstract fun newInstance(): TreeBuilder
    abstract fun parseFragment(
        inputFragment: String?,
        context: Element?,
        baseUri: String?,
        parser: Parser
    ): List<Node>?

    protected fun runParser() {
        val tokeniser: Tokeniser? = tokeniser
        val eof: Token.TokenType = Token.TokenType.EOF
        while (true) {
            val token: Token? = tokeniser!!.read()
            process((token)!!)
            token!!.reset()
            if (token.type == eof) break
        }
    }

    abstract fun process(token: Token): Boolean
    fun processStartTag(name: String?): Boolean {
        // these are "virtual" start tags (auto-created by the treebuilder), so not tracking the start position
        val start: Token.StartTag = start
        if (currentToken === start) { // don't recycle an in-use token
            return process(Token.StartTag().name(name))
        }
        return process(start.reset().name(name))
    }

    fun processStartTag(name: String?, attrs: Attributes?): Boolean {
        val start: Token.StartTag = start
        if (currentToken === start) { // don't recycle an in-use token
            return process(Token.StartTag().nameAttr(name, attrs))
        }
        start.reset()
        start.nameAttr(name, attrs)
        return process(start)
    }

    fun processEndTag(name: String?): Boolean {
        if (currentToken === end) { // don't recycle an in-use token
            return process(Token.EndTag().name(name))
        }
        return process(end.reset().name(name))
    }

    /**
     * Get the current element (last on the stack). If all items have been removed, returns the document instead
     * (which might not actually be on the stack; use stack.size() == 0 to test if required.
     * @return the last element on the stack, if any; or the root document
     */
    fun currentElement(): Element {
        val size: Int = stack!!.size
        return if (size > 0) stack!!.get(size - 1) else (doc)!!
    }

    /**
     * Checks if the Current Element's normal name equals the supplied name.
     * @param normalName name to check
     * @return true if there is a current element on the stack, and its name equals the supplied
     */
    fun currentElementIs(normalName: String): Boolean {
        if (stack!!.size == 0) return false
        val current: Element? = currentElement()
        return current != null && (current.normalName() == normalName)
    }

    /**
     * If the parser is tracking errors, add an error at the current position.
     * @param msg error message
     */
    protected fun error(msg: String?) {
        error(msg, *(null as Array<Any?>?)!!)
    }

    /**
     * If the parser is tracking errors, add an error at the current position.
     * @param msg error message template
     * @param args template arguments
     */
    protected fun error(msg: String?, vararg args: Any?) {
        val errors: ParseErrorList? = parser?.errors
        if (errors!!.canAddError()) errors.add(ParseError(reader, msg, *args))
    }

    /**
     * (An internal method, visible for Element. For HTML parse, signals that script and style text should be treated as
     * Data Nodes).
     */
    open fun isContentForTagData(normalName: String?): Boolean {
        return false
    }

    fun tagFor(tagName: String, settings: ParseSettings?): Tag? {
        var tag: Tag? =
            seenTags!!.get(tagName) // note that we don't normalize the cache key. But tag via valueOf may be normalized.
        if (tag == null) {
            tag = Tag.Companion.valueOf(tagName, settings)
            seenTags!!.put(tagName, tag)
        }
        return tag
    }

    /**
     * Called by implementing TreeBuilders when a node has been inserted. This implementation includes optionally tracking
     * the source range of the node.
     * @param node the node that was just inserted
     * @param token the (optional) token that created this node
     */
    fun onNodeInserted(node: Node, token: Token?) {
        trackNodePosition(node, token, true)
    }

    /**
     * Called by implementing TreeBuilders when a node is explicitly closed. This implementation includes optionally
     * tracking the closing source range of the node.
     * @param node the node being closed
     * @param token the end-tag token that closed this node
     */
    protected fun onNodeClosed(node: Node, token: Token?) {
        trackNodePosition(node, token, false)
    }

    private fun trackNodePosition(node: Node, token: Token?, start: Boolean) {
        if (trackSourceRange && token != null) {
            val startPos: Int = token.startPos()
            if (startPos == Token.Companion.Unset) return  // untracked, virtual token
            val startRange: Range.Position =
                Range.Position(startPos, reader!!.lineNumber(startPos), reader!!.columnNumber(startPos))
            val endPos: Int = token.endPos()
            val endRange: Range.Position =
                Range.Position(endPos, reader!!.lineNumber(endPos), reader!!.columnNumber(endPos))
            val range: Range = Range(startRange, endRange)
            range.track(node, start)
        }
    }
}
