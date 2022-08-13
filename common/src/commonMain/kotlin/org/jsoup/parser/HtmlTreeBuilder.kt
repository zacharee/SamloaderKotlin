package org.jsoup.parser

import com.soywiz.korio.lang.assert
import org.jsoup.helper.Validate
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.*

/**
 * HTML Tree Builder; creates a DOM from Tokens.
 */
class HtmlTreeBuilder : TreeBuilder() {
    private var state // the current state
            : HtmlTreeBuilderState? = null
    private var originalState // original / marked state
            : HtmlTreeBuilderState? = null
    private var baseUriSetFromDoc: Boolean = false

    var headElement // the current head element
            : Element? = null

    var formElement // the current form element
            : FormElement? = null

    private var contextElement // fragment parse context -- could be null even if fragment parsing
            : Element? = null
    private var formattingElements // active (open) formatting elements
            : ArrayList<Element?> = arrayListOf()
    private var tmplInsertMode = ArrayList<HtmlTreeBuilderState>()
    var pendingTableCharacters = ArrayList<String>()
    private var emptyEnd // reused empty end tag
            : Token.EndTag? = null
    private var framesetOk // if ok to go into frameset
            : Boolean = false
    var isFosterInserts // if next inserts should be fostered
            : Boolean = false
    var isFragmentParsing // if parsing a fragment of html
            : Boolean = false
        private set

    override fun defaultSettings(): ParseSettings {
        return ParseSettings.htmlDefault
    }

    override fun newInstance(): HtmlTreeBuilder {
        return HtmlTreeBuilder()
    }

    override fun initialiseParse(input: CharacterReader, baseUri: String, parser: Parser) {
        super.initialiseParse(input, baseUri, parser)

        // this is a bit mucky. todo - probably just create new parser objects to ensure all reset.
        state = HtmlTreeBuilderState.Initial
        originalState = null
        baseUriSetFromDoc = false
        headElement = null
        formElement = null
        contextElement = null
        emptyEnd = Token.EndTag()
        framesetOk = true
        isFosterInserts = false
        isFragmentParsing = false
    }

    override fun parseFragment(
        inputFragment: String?,
         context: Element?,
        baseUri: String,
        parser: Parser
    ): List<Node> {
        // context may be null
        state = HtmlTreeBuilderState.Initial
        initialiseParse(CharacterReader(inputFragment!!), baseUri, parser)
        contextElement = context
        isFragmentParsing = true
        var root: Element? = null
        if (context != null) {
            if (context.ownerDocument() != null) // quirks setup:
                doc!!.quirksMode(context.ownerDocument()!!.quirksMode())

            // initialise the tokeniser state:
            val contextTag: String = context.normalName()
            when (contextTag) {
                "title", "textarea" -> tokeniser!!.transition(TokeniserState.Rcdata)
                "iframe", "noembed", "noframes", "style", "xml" -> tokeniser!!.transition(TokeniserState.Rawtext)
                "script" -> tokeniser!!.transition(TokeniserState.ScriptData)
                "noscript" -> tokeniser!!.transition(TokeniserState.Data) // if scripting enabled, rawtext
                "plaintext" -> tokeniser!!.transition(TokeniserState.PLAINTEXT)
                "template" -> {
                    tokeniser!!.transition(TokeniserState.Data)
                    pushTemplateMode(HtmlTreeBuilderState.InTemplate)
                }

                else -> tokeniser!!.transition(TokeniserState.Data)
            }
            root = Element(tagFor((contextTag), settings), baseUri)
            doc!!.appendChild(root)
            stack.add(root)
            resetInsertionMode()

            // setup form element to nearest form on context (up ancestor chain). ensures form controls are associated
            // with form correctly
            var formSearch: Element? = context
            while (formSearch != null) {
                if (formSearch is FormElement) {
                    formElement = formSearch
                    break
                }
                formSearch = formSearch.parent()
            }
        }
        runParser()
        return if (context != null) {
            // depending on context and the input html, content may have been added outside of the root el
            // e.g. context=p, input=div, the div will have been pushed out.
            val nodes: List<Node> = root!!.siblingNodes()
            if (nodes.isNotEmpty()) root.insertChildren(-1, nodes)
            root.childNodes()
        } else doc!!.childNodes()
    }

    override fun process(token: Token): Boolean {
        currentToken = token
        return state!!.process(token, this)
    }

    fun process(token: Token, state: HtmlTreeBuilderState): Boolean {
        currentToken = token
        return state.process(token, this)
    }

    fun transition(state: HtmlTreeBuilderState?) {
        this.state = state
    }

    fun state(): HtmlTreeBuilderState? {
        return state
    }

    fun markInsertionMode() {
        originalState = state
    }

    fun originalState(): HtmlTreeBuilderState? {
        return originalState
    }

    fun framesetOk(framesetOk: Boolean) {
        this.framesetOk = framesetOk
    }

    fun framesetOk(): Boolean {
        return framesetOk
    }

    val document: Document
        get() {
            return (doc)!!
        }

    override var baseUri: String?
        get() {
            return super.baseUri
        }
        set(baseUri) {
            super.baseUri = baseUri
        }

    fun maybeSetBaseUri(base: Element?) {
        if (baseUriSetFromDoc) // only listen to the first <base href> in parse
            return
        val href: String? = base?.absUrl("href")
        if (!href.isNullOrEmpty()) { // ignore <base target> etc
            baseUri = (href)
            baseUriSetFromDoc = true
            doc!!.setBaseUri(href) // set on the doc so doc.createElement(Tag) will get updated base, and to update all descendants
        }
    }

    fun error(state: HtmlTreeBuilderState?) {
        if (parser?.errors?.canAddError() == true) parser?.errors?.add(
            ParseError(
                reader, "Unexpected %s token [%s] when in state [%s]",
                currentToken!!.tokenType(), currentToken, state
            )
        )
    }

    fun insert(startTag: Token.StartTag?): Element {
        // cleanup duplicate attributes:
        if (startTag!!.hasAttributes() && startTag.attributes?.isEmpty == false) {
            val dupes: Int = startTag.attributes!!.deduplicate((settings)!!)
            if (dupes > 0) {
                error("Dropped duplicate attribute(s) in tag [%s]", startTag.normName)
            }
        }

        // handle empty unknown tags
        // when the spec expects an empty tag, will directly hit insertEmpty, so won't generate this fake end tag.
        if (startTag.isSelfClosing) {
            val el: Element = insertEmpty(startTag)
            stack.add(el)
            tokeniser!!.transition(TokeniserState.Data) // handles <script />, otherwise needs breakout steps from script data
            tokeniser!!.emit(
                emptyEnd!!.reset().name(el.tagName())
            ) // ensure we get out of whatever state we are in. emitted for yielded processing
            return el
        }
        val el = Element(
            tagFor((startTag.name())!!, settings), null, settings!!.normalizeAttributes(
                startTag.attributes
            )
        )
        insert(el, startTag)
        return el
    }

    fun insertStartTag(startTagName: String?): Element {
        val el = Element(tagFor((startTagName)!!, settings), null)
        insert(el)
        return el
    }

    fun insert(el: Element) {
        insertNode(el, null)
        stack.add(el)
    }

    private fun insert(el: Element,  token: Token?) {
        insertNode(el, token)
        stack.add(el)
    }

    fun insertEmpty(startTag: Token.StartTag?): Element {
        val tag: Tag = tagFor((startTag!!.name())!!, settings)
        val el = Element(tag, null, settings!!.normalizeAttributes(startTag.attributes))
        insertNode(el, startTag)
        if (startTag.isSelfClosing) {
            if (tag.isKnownTag) {
                if (!tag.isEmpty) tokeniser!!.error(
                    "Tag [%s] cannot be self closing; not a void tag",
                    tag.normalName()
                )
            } else  // unknown tag, remember this is self closing for output
                tag.setSelfClosing()
        }
        return el
    }

    fun insertForm(startTag: Token.StartTag?, onStack: Boolean, checkTemplateStack: Boolean): FormElement {
        val tag: Tag = tagFor((startTag!!.name())!!, settings)
        val el = FormElement(tag, null, settings!!.normalizeAttributes(startTag.attributes))
        if (checkTemplateStack) {
            if (!onStack("template")) formElement = el
        } else formElement = el
        insertNode(el, startTag)
        if (onStack) stack.add(el)
        return el
    }

    fun insert(commentToken: Token.Comment?) {
        val comment = Comment(commentToken!!.getData())
        insertNode(comment, commentToken)
    }

    fun insert(characterToken: Token.Character?) {
        val el = currentElement() // will be doc if no current element; allows for whitespace to be inserted into the doc root object (not on the stack)
        val tagName: String? = el?.normalName()
        val data: String? = characterToken?.data
        val node = if (characterToken?.isCData == true) CDataNode(data) else if (isContentForTagData(tagName)) DataNode((data)!!) else TextNode((data)!!)
        el?.appendChild(node) // doesn't use insertNode, because we don't foster these; and will always have a stack.
        onNodeInserted(node, characterToken)
    }

    private fun insertNode(node: Node,  token: Token?) {
        // if the stack hasn't been set up yet, elements (doctype, comments) go into the doc
        if (stack.isEmpty()) {
            doc!!.appendChild(node)
        } else if (isFosterInserts && StringUtil.inSorted(
                currentElement()?.normalName(),
                HtmlTreeBuilderState.Constants.InTableFoster
        )) {
            insertInFosterParent(node)
        } else {
            currentElement()?.appendChild(node)
        }

        // connect form controls to their form element
        if (node is Element && node.tag().isFormListed) {
            if (formElement != null) formElement!!.addElement(node)
        }
        onNodeInserted(node, token)
    }

    fun pop(): Element {
        val size: Int = stack.size
        return (stack.removeAt(size - 1))
    }

    fun push(element: Element) {
        stack.add(element)
    }

    fun onStack(el: Element): Boolean {
        return onStack(stack, el)
    }

    fun onStack(elName: String?): Boolean {
        return getFromStack(elName) != null
    }


    fun getFromStack(elName: String?): Element? {
        val bottom: Int = stack.size - 1
        val upper: Int = if (bottom >= maxQueueDepth) bottom - maxQueueDepth else 0
        for (pos in bottom downTo upper) {
            val next: Element = (stack[pos])
            if ((next.normalName() == elName)) {
                return next
            }
        }
        return null
    }

    fun removeFromStack(el: Element?): Boolean {
        for (pos in stack.indices.reversed()) {
            val next: Element = (stack[pos])
            if (next === el) {
                stack.removeAt(pos)
                return true
            }
        }
        return false
    }


    fun popStackToClose(elName: String?): Element? {
        for (pos in stack.indices.reversed()) {
            val el: Element = (stack[pos])
            stack.removeAt(pos)
            if ((el.normalName() == elName)) {
                if (currentToken is Token.EndTag) onNodeClosed(el, currentToken)
                return el
            }
        }
        return null
    }

    // elnames is sorted, comes from Constants
    fun popStackToClose(vararg elNames: String?) {
        for (pos in stack.indices.reversed()) {
            val next: Element = (stack[pos])
            stack.removeAt(pos)
            if (StringUtil.inSorted(next.normalName(), elNames)) break
        }
    }

    fun popStackToBefore(elName: String) {
        for (pos in stack.indices.reversed()) {
            val next: Element = (stack[pos])
            if ((next.normalName() == elName)) {
                break
            } else {
                stack.removeAt(pos)
            }
        }
    }

    fun clearStackToTableContext() {
        clearStackToContext("table", "template")
    }

    fun clearStackToTableBodyContext() {
        clearStackToContext("tbody", "tfoot", "thead", "template")
    }

    fun clearStackToTableRowContext() {
        clearStackToContext("tr", "template")
    }

    private fun clearStackToContext(vararg nodeNames: String) {
        for (pos in stack.indices.reversed()) {
            val next: Element = (stack[pos])
            if (StringUtil.`in`(
                    next.normalName(),
                    *nodeNames
                ) || (next.normalName() == "html")
            ) break else stack.removeAt(pos)
        }
    }


    fun aboveOnStack(el: Element?): Element? {
        if (el == null) return null

        assert(onStack(el))
        for (pos in stack.indices.reversed()) {
            val next: Element = (stack[pos])
            if (next === el) {
                return stack[pos - 1]
            }
        }
        return null
    }

    fun insertOnStackAfter(after: Element, `in`: Element) {
        val i: Int = stack.lastIndexOf(after)
        Validate.isTrue(i != -1)
        stack.add(i + 1, `in`)
    }

    fun replaceOnStack(out: Element, `in`: Element) {
        replaceInQueue(stack, out, `in`)
    }

    private fun <T : Element?> replaceInQueue(queue: ArrayList<T>, out: T?, `in`: T) {
        val i: Int = queue.lastIndexOf(out)
        Validate.isTrue(i != -1)
        queue[i] = `in`
    }

    /**
     * Reset the insertion mode, by searching up the stack for an appropriate insertion mode. The stack search depth
     * is limited to [.maxQueueDepth].
     * @return true if the insertion mode was actually changed.
     */
    fun resetInsertionMode(): Boolean {
        // https://html.spec.whatwg.org/multipage/parsing.html#the-insertion-mode
        var last = false
        val bottom: Int = stack.size - 1
        val upper: Int = if (bottom >= maxQueueDepth) bottom - maxQueueDepth else 0
        val origState: HtmlTreeBuilderState? = state
        if (stack.size == 0) { // nothing left of stack, just get to body
            transition(HtmlTreeBuilderState.InBody)
        }
        LOOP@ for (pos in bottom downTo upper) {
            var node: Element? = stack[pos]
            if (pos == upper) {
                last = true
                if (isFragmentParsing) node = contextElement
            }
            when (node?.normalName() ?: "") {
                "select" -> {
                    transition(HtmlTreeBuilderState.InSelect)
                    // todo - should loop up (with some limit) and check for table or template hits
                    break@LOOP
                }

                "td", "th" -> if (!last) {
                    transition(HtmlTreeBuilderState.InCell)
                    break@LOOP
                }

                "tr" -> {
                    transition(HtmlTreeBuilderState.InRow)
                    break@LOOP
                }

                "tbody", "thead", "tfoot" -> {
                    transition(HtmlTreeBuilderState.InTableBody)
                    break@LOOP
                }

                "caption" -> {
                    transition(HtmlTreeBuilderState.InCaption)
                    break@LOOP
                }

                "colgroup" -> {
                    transition(HtmlTreeBuilderState.InColumnGroup)
                    break@LOOP
                }

                "table" -> {
                    transition(HtmlTreeBuilderState.InTable)
                    break@LOOP
                }

                "template" -> {
                    val tmplState: HtmlTreeBuilderState? = currentTemplateMode()
                    Validate.notNull(tmplState, "Bug: no template insertion mode on stack!")
                    transition(tmplState)
                    break@LOOP
                }

                "head" -> if (!last) {
                    transition(HtmlTreeBuilderState.InHead)
                    break@LOOP
                }

                "body" -> {
                    transition(HtmlTreeBuilderState.InBody)
                    break@LOOP
                }

                "frameset" -> {
                    transition(HtmlTreeBuilderState.InFrameset)
                    break@LOOP
                }

                "html" -> {
                    transition(if (headElement == null) HtmlTreeBuilderState.BeforeHead else HtmlTreeBuilderState.AfterHead)
                    break@LOOP
                }
            }
            if (last) {
                transition(HtmlTreeBuilderState.InBody)
                break
            }
        }
        return state !== origState
    }

    /** Places the body back onto the stack and moves to InBody, for cases in AfterBody / AfterAfterBody when more content comes  */
    fun resetBody() {
        if (!onStack("body")) {
            stack.add(doc!!.body())
        }
        transition(HtmlTreeBuilderState.InBody)
    }

    // todo: tidy up in specific scope methods
    private val specificScopeTarget: Array<String?> = arrayOf(null)
    private fun inSpecificScope(targetName: String?, baseTypes: Array<String>, extraTypes: Array<String>?): Boolean {
        specificScopeTarget[0] = targetName
        return inSpecificScope(specificScopeTarget, baseTypes, extraTypes)
    }

    private fun inSpecificScope(
        targetNames: Array<out String?>,
        baseTypes: Array<String>,
        extraTypes: Array<String>?
    ): Boolean {
        // https://html.spec.whatwg.org/multipage/parsing.html#has-an-element-in-the-specific-scope
        val bottom: Int = stack.size - 1
        val top: Int = if (bottom > MaxScopeSearchDepth) bottom - MaxScopeSearchDepth else 0
        // don't walk too far up the tree
        for (pos in bottom downTo top) {
            val elName: String = stack[pos].normalName()
            if (StringUtil.inSorted(elName, targetNames)) return true
            if (StringUtil.inSorted(elName, baseTypes)) return false
            if (extraTypes != null && StringUtil.inSorted(elName, extraTypes)) return false
        }
        //Validate.fail("Should not be reachable"); // would end up false because hitting 'html' at root (basetypes)
        return false
    }

    fun inScope(targetNames: Array<out String?>): Boolean {
        return inSpecificScope(targetNames, TagsSearchInScope, null)
    }

    fun inScope(targetName: String?, extras: Array<String>? = null): Boolean {
        return inSpecificScope(targetName, TagsSearchInScope, extras)
        // todo: in mathml namespace: mi, mo, mn, ms, mtext annotation-xml
        // todo: in svg namespace: forignOjbect, desc, title
    }

    fun inListItemScope(targetName: String?): Boolean {
        return inScope(targetName, TagSearchList)
    }

    fun inButtonScope(targetName: String?): Boolean {
        return inScope(targetName, TagSearchButton)
    }

    fun inTableScope(targetName: String?): Boolean {
        return inSpecificScope(targetName, TagSearchTableScope, null)
    }

    fun inSelectScope(targetName: String): Boolean {
        for (pos in stack.indices.reversed()) {
            val el: Element = (stack[pos])
            val elName: String = el.normalName()
            if ((elName == targetName)) return true
            if (!StringUtil.inSorted(elName, TagSearchSelectScope)) // all elements except
                return false
        }
        Validate.fail("Should not be reachable")
        return false
    }

    fun newPendingTableCharacters() {
        pendingTableCharacters = ArrayList()
    }

    /**
     * 13.2.6.3 Closing elements that have implied end tags
     * When the steps below require the UA to generate implied end tags, then, while the current node is a dd element, a dt element, an li element, an optgroup element, an option element, a p element, an rb element, an rp element, an rt element, or an rtc element, the UA must pop the current node off the stack of open elements.
     *
     * If a step requires the UA to generate implied end tags but lists an element to exclude from the process, then the UA must perform the above steps as if that element was not in the above list.
     *
     * When the steps below require the UA to generate all implied end tags thoroughly, then, while the current node is a caption element, a colgroup element, a dd element, a dt element, an li element, an optgroup element, an option element, a p element, an rb element, an rp element, an rt element, an rtc element, a tbody element, a td element, a tfoot element, a th element, a thead element, or a tr element, the UA must pop the current node off the stack of open elements.
     *
     * @param excludeTag If a step requires the UA to generate implied end tags but lists an element to exclude from the
     * process, then the UA must perform the above steps as if that element was not in the above list.
     */
    fun generateImpliedEndTags(excludeTag: String?) {
        while (StringUtil.inSorted(currentElement()?.normalName(), TagSearchEndTags)) {
            if (excludeTag != null && currentElementIs(excludeTag)) break
            pop()
        }
    }

    /**
     * Pops elements off the stack according to the implied end tag rules
     * @param thorough if we are thorough (includes table elements etc) or not
     */
    fun generateImpliedEndTags(thorough: Boolean = false) {
        val search: Array<String> = if (thorough) TagThoroughSearchEndTags else TagSearchEndTags
        while (StringUtil.inSorted(currentElement()?.normalName(), search)) {
            pop()
        }
    }

    fun closeElement(name: String) {
        generateImpliedEndTags(name)
        if (name != currentElement()?.normalName()) error(state())
        popStackToClose(name)
    }

    fun isSpecial(el: Element?): Boolean {
        // todo: mathml's mi, mo, mn
        // todo: svg's foreigObject, desc, title
        val name: String? = el!!.normalName()
        return StringUtil.inSorted(name, TagSearchSpecial)
    }

    fun lastFormattingElement(): Element? {
        return if (formattingElements.size > 0) formattingElements[formattingElements.size - 1] else null
    }

    fun positionOfElement(el: Element?): Int {
        for (i in formattingElements.indices) {
            if (el === formattingElements[i]) return i
        }
        return -1
    }

    fun removeLastFormattingElement(): Element? {
        val size: Int = formattingElements.size
        if (size > 0) return formattingElements.removeAt(size - 1) else return null
    }

    // active formatting elements
    fun pushActiveFormattingElements(`in`: Element?) {
        checkActiveFormattingElements(`in`)
        formattingElements.add(`in`)
    }

    fun pushWithBookmark(`in`: Element?, bookmark: Int) {
        checkActiveFormattingElements(`in`)
        // catch any range errors and assume bookmark is incorrect - saves a redundant range check.
        try {
            formattingElements.add(bookmark, `in`)
        } catch (e: IndexOutOfBoundsException) {
            formattingElements.add(`in`)
        }
    }

    fun checkActiveFormattingElements(`in`: Element?) {
        var numSeen = 0
        val size: Int = formattingElements.size - 1
        var ceil: Int = size - maxUsedFormattingElements
        if (ceil < 0) ceil = 0
        for (pos in size downTo ceil) {
            val el: Element = formattingElements[pos] ?: break
            if (isSameFormattingElement(`in`, el)) numSeen++
            if (numSeen == 3) {
                formattingElements.removeAt(pos)
                break
            }
        }
    }

    private fun isSameFormattingElement(a: Element?, b: Element): Boolean {
        // same if: same namespace, tag, and attributes. Element.equals only checks tag, might in future check children
        return (a!!.normalName() == b.normalName()) && (a.attributes() == b.attributes())
        // todo: namespaces
    }

    fun reconstructFormattingElements() {
        if (stack.size > maxQueueDepth) return
        val last: Element? = lastFormattingElement()
        if (last == null || onStack(last)) return
        var entry: Element? = last
        val size: Int = formattingElements.size
        var ceil: Int = size - maxUsedFormattingElements
        if (ceil < 0) ceil = 0
        var pos: Int = size - 1
        var skip = false
        while (true) {
            if (pos == ceil) { // step 4. if none before, skip to 8
                skip = true
                break
            }
            entry = formattingElements[--pos] // step 5. one earlier than entry
            if (entry == null || onStack(entry)) // step 6 - neither marker nor on stack
                break // jump to 8, else continue back to 4
        }
        while (true) {
            if (!skip) // step 7: on later than entry
                entry = formattingElements[++pos]
            Validate.notNull(entry) // should not occur, as we break at last element

            // 8. create new element from element, 9 insert into current node, onto stack
            skip = false // can only skip increment from 4.
            val newEl = Element(
                tagFor((entry!!.normalName()), settings), null, entry.attributes()
                    .clone()
            )
            insert(newEl)

            // 10. replace entry with new entry
            formattingElements[pos] = newEl

            // 11
            if (pos == size - 1) // if not last entry in list, jump to 7
                break
        }
    }

    fun clearFormattingElementsToLastMarker() {
        while (!formattingElements.isEmpty()) {
            removeLastFormattingElement() ?: break
        }
    }

    fun removeFromActiveFormattingElements(el: Element) {
        for (pos in formattingElements.indices.reversed()) {
            val next: Element? = formattingElements[pos]
            if (next === el) {
                formattingElements.removeAt(pos)
                break
            }
        }
    }

    fun isInActiveFormattingElements(el: Element?): Boolean {
        return onStack(formattingElements, el)
    }

    fun getActiveFormattingElement(nodeName: String?): Element? {
        for (pos in formattingElements.indices.reversed()) {
            val next: Element? = formattingElements[pos]
            if (next == null) // scope marker
                break else if ((next.normalName() == nodeName)) return next
        }
        return null
    }

    fun replaceActiveFormattingElement(out: Element?, `in`: Element) {
        replaceInQueue(formattingElements, out, `in`)
    }

    fun insertMarkerToFormattingElements() {
        formattingElements.add(null)
    }

    fun insertInFosterParent(`in`: Node) {
        val fosterParent: Element?
        val lastTable: Element? = getFromStack("table")
        var isLastTableParent = false
        if (lastTable != null) {
            if (lastTable.parent() != null) {
                fosterParent = lastTable.parent()
                isLastTableParent = true
            } else fosterParent = aboveOnStack(lastTable)
        } else { // no table == frag
            fosterParent = stack[0]
        }
        if (isLastTableParent) {
            Validate.notNull(lastTable) // last table cannot be null by this point.
            lastTable!!.before(`in`)
        } else fosterParent!!.appendChild(`in`)
    }

    // Template Insertion Mode stack
    fun pushTemplateMode(state: HtmlTreeBuilderState) {
        tmplInsertMode.add(state)
    }


    fun popTemplateMode(): HtmlTreeBuilderState? {
        return if (tmplInsertMode.size > 0) {
            tmplInsertMode.removeAt(tmplInsertMode.size - 1)
        } else {
            null
        }
    }

    fun templateModeSize(): Int {
        return tmplInsertMode.size
    }


    fun currentTemplateMode(): HtmlTreeBuilderState? {
        return if ((tmplInsertMode.size > 0)) tmplInsertMode[tmplInsertMode.size - 1] else null
    }

    override fun toString(): String {
        return ("TreeBuilder{" +
                "currentToken=" + currentToken +
                ", state=" + state +
                ", currentElement=" + currentElement() +
                '}')
    }

    override fun isContentForTagData(normalName: String?): Boolean {
        return ((normalName == "script") || (normalName == "style"))
    }

    companion object {
        // tag searches. must be sorted, used in inSorted. HtmlTreeBuilderTest validates they're sorted.
        val TagsSearchInScope: Array<String> =
            arrayOf("applet", "caption", "html", "marquee", "object", "table", "td", "th")
        val TagSearchList: Array<String> = arrayOf("ol", "ul")
        val TagSearchButton: Array<String> = arrayOf("button")
        val TagSearchTableScope: Array<String> = arrayOf("html", "table")
        val TagSearchSelectScope: Array<String> = arrayOf("optgroup", "option")
        val TagSearchEndTags: Array<String> =
            arrayOf("dd", "dt", "li", "optgroup", "option", "p", "rb", "rp", "rt", "rtc")
        val TagThoroughSearchEndTags: Array<String> = arrayOf(
            "caption",
            "colgroup",
            "dd",
            "dt",
            "li",
            "optgroup",
            "option",
            "p",
            "rb",
            "rp",
            "rt",
            "rtc",
            "tbody",
            "td",
            "tfoot",
            "th",
            "thead",
            "tr"
        )
        val TagSearchSpecial: Array<String> = arrayOf(
            "address", "applet", "area", "article", "aside", "base", "basefont", "bgsound",
            "blockquote", "body", "br", "button", "caption", "center", "col", "colgroup", "command", "dd",
            "details", "dir", "div", "dl", "dt", "embed", "fieldset", "figcaption", "figure", "footer", "form",
            "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header", "hgroup", "hr", "html",
            "iframe", "img", "input", "isindex", "li", "link", "listing", "marquee", "menu", "meta", "nav",
            "noembed", "noframes", "noscript", "object", "ol", "p", "param", "plaintext", "pre", "script",
            "section", "select", "style", "summary", "table", "tbody", "td", "textarea", "tfoot", "th", "thead",
            "title", "tr", "ul", "wbr", "xmp"
        )
        const val MaxScopeSearchDepth: Int = 100 // prevents the parser bogging down in exceptionally broken pages
        private const val maxQueueDepth: Int = 256 // an arbitrary tension point between real HTML and crafted pain
        private fun <T : Element?> onStack(queue: ArrayList<T>, element: T?): Boolean {
            val bottom: Int = queue.size - 1
            val upper: Int = if (bottom >= maxQueueDepth) bottom - maxQueueDepth else 0
            for (pos in bottom downTo upper) {
                val next: Element? = queue[pos]
                if (next === element) {
                    return true
                }
            }
            return false
        }

        private const val maxUsedFormattingElements: Int = 12 // limit how many elements get recreated
    }
}
