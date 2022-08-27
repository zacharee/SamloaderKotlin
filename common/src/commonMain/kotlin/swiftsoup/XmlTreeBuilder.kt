package swiftsoup

class XmlTreeBuilder : TreeBuilder() {
    override fun defaultSettings(): ParseSettings {
        return ParseSettings.preserveCase
    }

    fun parse(input: String, baseUri: String): Document {
        return parse(input, baseUri, ParseErrorList.noTracking(), ParseSettings.preserveCase)
    }

    override fun initializeParse(
        input: String,
        baseUri: String,
        errors: ParseErrorList,
        settings: ParseSettings
    ) {
        super.initializeParse(input, baseUri, errors, settings)
        stack.add(doc) // place the document onto the stack. differs from HtmlTreeBuilder (not on stack)
        doc.outputSettings.syntax = OutputSettings.Syntax.xml
    }

    override fun process(token: Token): Boolean {
        // start tag, end tag, doctype, comment, character, eof
        when (token) {
            is Token.StartTag -> insert(token)
            is Token.EndTag -> popStackToClose(token)
            is Token.Comment -> insert(token)
            is Token.Char -> insert(token)
            is Token.Doctype -> insert(token)
        }
        return true
    }

    private fun insertNode(node: Node) {
        currentElement()?.appendChild(node)
    }

    fun insert(startTag: Token.StartTag): Element {
        val tag: Tag = Tag.valueOf(startTag.tagName, settings)
        // todo: wonder if for xml parsing, should treat all tags as unknown? because it's not html.
        val el = Element(tag, baseUri, settings.normalizeAttributes(startTag.attributes))
        insertNode(el)
        if (startTag.selfClosing) {
            tokenizer.acknowledgeSelfClosingFlag()
            if (!tag.isKnownTag) // unknown tag, remember this is self closing for output. see above.
            {
                tag.selfClosing = true
            }
        } else {
            stack.add(el)
        }
        return el
    }

    fun insert(commentToken: Token.Comment) {
        val comment: Comment = Comment(commentToken.data, baseUri)
        var insert: Node = comment
        if (commentToken.bogus) { // xml declarations are emitted as bogus comments (which is right for html, but not xml)
            // so we do a bit of a hack and parse the data as an element to pull the attributes out
            val data: String = comment.getData()
            if (data.length > 1 && (data.startsWith("!") || data.startsWith("?"))) {
                val doc: Document = SwiftSoup.parse(
                    "<" + data.substring(
                        1,
                        data.length - 2
                    ) + ">", baseUri, Parser.xmlParser()
                )
                val el: Element = doc.child(0)
                insert = XmlDeclaration(settings.normalizeTag(el.tagName), comment.getBaseUri(), data.startsWith("!"))
                insert.attributes?.addAll(el.attributes)
            }
        }
        insertNode(insert)
    }

    fun insert(characterToken: Token.Char) {
        val node: Node = TextNode(characterToken.data!!, baseUri)
        insertNode(node)
    }

    fun insert(d: Token.Doctype) {
        val doctypeNode = DocumentType(
            settings.normalizeTag(d.name.toString()),
            d.pubSysKey,
            d.publicIdentifier,
            d.systemIdentifier,
            baseUri
        )
        insertNode(doctypeNode)
    }

    /**
     * If the stack contains an element with this tag's name, pop up the stack to remove the first occurrence. If not
     * found, skips.
     *
     * @param endTag
     */
    private fun popStackToClose(endTag: Token.EndTag) {
        val elName: String = endTag.tagName!!
        var firstFound: Element? = null

        for (pos in (stack.size - 1 downTo 0)) {
            val next: Element = stack[pos]
            if (next.nodeName() == elName) {
                firstFound = next

            }
        }
        if (firstFound == null) {
            return // not found, skip
        }

        for (pos in (stack.size - 1 downTo 0)) {
            val next: Element = stack[pos]
            stack.removeAt(pos)
            if (next == firstFound) {
                break
            }
        }
    }

    fun parseFragment(
        inputFragment: String,
        baseUri: String,
        errors: ParseErrorList,
        settings: ParseSettings
    ): List<Node> {
        initialiseParse(inputFragment, baseUri, errors, settings)
        runParser()
        return doc.childNodes
    }
}
