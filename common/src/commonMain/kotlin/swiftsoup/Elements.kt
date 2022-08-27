package swiftsoup

open class Elements(private val list: ArrayList<Element> = arrayListOf()) {
    fun copy(): Elements {
        val clone = Elements()
        list.forEach {
            clone.add(it.copy() as Element)
        }

        return clone
    }

    fun attr(attributeKey: String): String {
        return list.find { it.hasAttr(attributeKey) }?.attr(attributeKey) ?: ""
    }

    fun hasAttr(attributeKey: String): Boolean {
        return list.any { it.hasAttr(attributeKey) }
    }

    fun attr(attributeKey: String, attributeValue: String): Elements {
        list.forEach {
            it.attr(attributeKey, attributeValue)
        }
        return this
    }

    fun removeAttr(attributeKey: String): Elements {
        list.forEach { it.removeAttr(attributeKey) }
        return this
    }

    fun addClass(className: String): Elements {
        list.forEach { it.addClass(className) }
        return this
    }

    fun removeClass(className: String): Elements {
        list.forEach { it.removeClass(className) }
        return this
    }

    fun toggleClass(className: String): Elements {
        list.forEach { it.toggleClass(className) }
        return this
    }

    fun hasClass(className: String): Boolean = list.any { it.hasClass(className) }

    fun `val`(): String = if (size() > 0) first()!!.`val`() else ""

    fun `val`(value: String): Elements {
        list.forEach { it.`val`(value) }
        return this
    }

    fun text(trimAndNormalizeWhitespace: Boolean = true): String {
        val sb = StringBuilder()
        list.forEach {
            if (sb.isNotEmpty()) {
                sb.append(" ")
            }

            sb.append(it.text(trimAndNormalizeWhitespace))
        }
        return sb.toString()
    }

    fun hasText(): Boolean = list.any { it.hasText() }

    fun eachText(): List<String> {
        val texts = arrayListOf<String>()

        list.forEach { if (it.hasText()) texts.add(it.text()) }

        return texts
    }

    fun html(): String {

    }

    fun outerHtml(): String {

    }

    override fun toString(): String {
        return outerHtml()
    }

    fun tagName(tagName: String): Elements {

    }

    fun html(html: String): Elements {

    }

    fun prepend(html: String): Elements {

    }

    fun append(html: String): Elements {

    }

    fun before(html: String): Elements {

    }

    fun after(html: String): Elements {

    }

    fun wrap(html: String): Elements {

    }

    fun unwrap(): Elements {

    }

    fun empty(): Elements {

    }

    fun remove(): Elements {

    }

    fun select(query: String): Elements {

    }

    fun not(query: String): Elements {

    }

    fun iS(query: String): Boolean {

    }

    fun parents(): Elements {

    }

    fun first() = if (isEmpty) null else get(0)

    val isEmpty: Boolean
        get() = list.isEmpty()

    val size: Int
        get() = list.size

    fun last(): Element? = if (isEmpty) null else get(size - 1)

    fun traverse(nodeVisitor: NodeVisitor): Elements {

    }

    fun forms(): List<FormElement> {

    }

    fun add(e: Element) {

    }

    fun add(index: Int, element: Element) {

    }

    operator fun get(i: Int): Element {

    }

    fun list() = list
}
