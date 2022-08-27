package swiftsoup

class HtmlTreeBuilder : TreeBuilder() {
    companion object TagSets {
        val inScope = arrayOf(
            "applet", "caption", "html", "table", "td", "th", "marquee", "object"
        )
        val list = arrayOf("ol", "ul")
        val button = arrayOf("button")
        val tableScope = arrayOf("html", "table")
        val selectScope = arrayOf("optgroup", "option")
        val endTags = arrayOf("dd", "dt", "li", "option", "optgroup", "p", "rp", "rt")
        val titleTextarea = arrayOf("title", "textarea")
        val frames = arrayOf("iframe", "noembed", "noframes", "style", "xmp")
        val special = setOf(
            "address", "applet", "area", "article", "aside", "base", "basefont", "bgsound",
            "blockquote", "body", "br", "button", "caption", "center", "col", "colgroup", "command", "dd",
            "details", "dir", "div", "dl", "dt", "embed", "fieldset", "figcaption", "figure", "footer", "form",
            "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header", "hgroup", "hr", "html",
            "iframe", "img", "input", "isindex", "li", "link", "listing", "marquee", "menu", "meta", "nav",
            "noembed", "noframes", "noscript", "object", "ol", "p", "param", "plaintext", "pre", "script",
            "section", "select", "style", "summary", "table", "tbody", "td", "textarea", "tfoot", "th", "thead",
            "title", "tr", "ul", "wbr", "xmp"
        )
    }

    var state = HtmlTreeBuilderState.Initial
        private set
    var originalState = HtmlTreeBuilderState.Initial
        private set

    private var baseUriSetFromDoc = false
    var headElement: Element? = null
    var formElement: FormElement? = null
    private var contextElement: Element? = null
    private val formattingElements = arrayListOf<Element?>()
    var pendingTableCharacters = arrayListOf<String>()
    private var emptyEnd = Token.EndTag()

    var framesetOk = true
    var fosterInserts = false
    var fragmentParsing = false

    override fun defaultSettings(): ParseSettings {
        return ParseSettings.htmlDefault
    }

    override fun parse(input: String, baseUri: String, errors: ParseErrorList, settings: ParseSettings): Document {
        state = HtmlTreeBuilderState.Initial
        baseUriSetFromDoc = false
        return super.parse(input, baseUri, errors, settings)
    }

    fun parseFragment(inputFragment: String, context: Element?, baseUri: String, errors: ParseErrorList, settings: ParseSettings): List<Node> {
        state = HtmlTreeBuilderState.Initial
        initializeParse(inputFragment, baseUri, errors, settings)
    }

    override fun process(token: Token): Boolean {

    }

    fun process(token: Token, state: HtmlTreeBuilderState): Boolean {

    }

    fun transition(state: HtmlTreeBuilderState) {

    }

    fun markInsertionMode() {
        originalState = state
    }

    fun maybeSetBaseUri(base: Element) {
        if (baseUriSetFromDoc) {
            return
        }

        val href = base.absUrl("href")
        if (href.isNotEmpty()) {
            baseUri = href
            baseUriSetFromDoc = true
            doc.setBaseUri(href)
        }
    }

    fun error(state: HtmlTreeBuilderState) {
        if (errors.canAddError && currentToken != null) {
            errors.add(ParseError(reader.pos, "Unexpected token [${(currentToken!!.type)}] when in state [${state.rawValue)}"))
        }
    }

    fun insert(startTag: Token.StartTag): Element {

    }

    fun insertStartTag(startTagName: String): Element {

    }

    fun insert(el: Element) {

    }

    fun insertEmpty(startTag: Token.StartTag): Element {

    }

    fun insertForm(startTag: Token.StartTag, onStack: Boolean): FormElement {

    }

    fun insert(commentToken: Token.Comment) {

    }

    fun insert(characterToken: Token.Char) {

    }

    private fun insertNode(node: Node) {

    }

    fun pop(): Element {

    }

    fun push(element: Element) {

    }

    fun onStack(el: Element): Boolean {

    }

    private fun isElementInQueue(queue: ArrayList<Element?>, element: Element?): Boolean {

    }

    fun getFromStack(elName: String): Element? {

    }

    fun removeFromStack(el: Element): Boolean {

    }

    fun popStackToClose(elName: String) {

    }

    fun popStackToClose(vararg elNames: String) {

    }

    fun popStackToBefore(elName: String) {

    }

    fun clearStackToTableContext() {

    }

    fun clearStackToTableBodyContext() {

    }

    fun clearStackToTableRowContext() {

    }

    fun clearStackToContext(vararg nodeNames: String) {

    }

    fun aboveOnStack(el: Element): Element? {

    }

    fun insertOnStackAfter(after: Element, input: Element) {

    }

    fun replaceOnStack(out: Element, input: Element) {

    }

    private fun replaceInQueue(queue: ArrayList<Element?>, out: Element, input: Element): ArrayList<Element> {

    }

    private fun replaceInQueue(queue: ArrayList<Element>, out: Element, input: Element): ArrayList<Element?> {

    }

    fun resetInsertionMode() {

    }

    private fun inSpecificScope(targetNames: Array<String>, baseTypes: Array<String>, extraTypes: Array<String>? = null): Boolean {

    }

    fun inScope(targetNames: Array<String>): Boolean {

    }

    fun inScope(targetName: String, extras: Array<String>? = null): Boolean {

    }

    fun inListItemScope(targetName: String): Boolean {

    }

    fun inButtonScope(targetName: String): Boolean {

    }

    fun inSelectScope(targetName: String): Boolean {

    }

    fun generateImpliedEndTags(excludeTag: String? = null) {

    }

    fun isSpecial(el: Element): Boolean {

    }

    fun lastFormattingElement(): Element? {

    }

    fun removeLastFormattingElement(): Element? {

    }

    fun pushActiveFormattingElements(input: Element) {

    }

    private fun isSameFormattingElement(a: Element, b: Element): Boolean {

    }

    fun reconstructFormattingElements() {

    }

    fun clearFormattingElementsToLastMarker() {

    }

    fun removeFromActiveFormattingElements(el: Element?) {

    }

    fun isInActiveFormattingElements(el: Element): Boolean {

    }

    fun getActiveFormattingElement(nodeName: String): Element? {

    }

    fun replaceActiveFormattingElement(out: Element, input: Element) {

    }

    fun insertMarkerToFormattingElements() {

    }

    fun insertInFosterParent(input: Node) {

    }
}
