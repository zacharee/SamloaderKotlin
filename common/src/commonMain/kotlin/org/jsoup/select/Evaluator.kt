package org.jsoup.select

import com.soywiz.korio.lang.format
import org.jsoup.format
import org.jsoup.helper.Validate
import org.jsoup.internal.Normalizer
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.*
import org.jsoup.parser.Tag.Companion.valueOf

/**
 * Evaluates that an element matches the selector.
 */
abstract class Evaluator protected constructor() {
    /**
     * Test if the element meets the evaluator's requirements.
     *
     * @param root    Root of the matching subtree
     * @param element tested element
     * @return Returns <tt>true</tt> if the requirements are met or
     * <tt>false</tt> otherwise
     */
    abstract fun matches(root: Element, element: Element): Boolean

    /**
     * Evaluator for tag name
     */
    class Tag constructor(private val tagName: String?) : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            return ((element.normalName() == tagName))
        }

        public override fun toString(): String {
            return String.format("%s", tagName)
        }
    }

    /**
     * Evaluator for tag name that ends with
     */
    class TagEndsWith constructor(private val tagName: String) : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            return (element.normalName()!!.endsWith(tagName))
        }

        public override fun toString(): String {
            return String.format("%s", tagName)
        }
    }

    /**
     * Evaluator for element id
     */
    class Id constructor(private val id: String?) : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            return ((id == element.id()))
        }

        public override fun toString(): String {
            return String.format("#%s", id)
        }
    }

    /**
     * Evaluator for element class
     */
    class Class constructor(private val className: String) : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            return (element.hasClass(className))
        }

        public override fun toString(): String {
            return String.format(".%s", className)
        }
    }

    /**
     * Evaluator for attribute name matching
     */
    class Attribute constructor(private val key: String?) : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            return element.hasAttr(key ?: return false)
        }

        public override fun toString(): String {
            return String.format("[%s]", key)
        }
    }

    /**
     * Evaluator for attribute name prefix matching
     */
    class AttributeStarting constructor(keyPrefix: String?) : Evaluator() {
        private val keyPrefix: String?

        init {
            Validate.notEmpty(keyPrefix)
            this.keyPrefix = Normalizer.lowerCase(keyPrefix)
        }

        public override fun matches(root: Element, element: Element): Boolean {
            val values: List<org.jsoup.nodes.Attribute?> = element.attributes()!!.asList()
            for (attribute: org.jsoup.nodes.Attribute? in values) {
                if (Normalizer.lowerCase(attribute?.key).startsWith(keyPrefix!!)) return true
            }
            return false
        }

        public override fun toString(): String {
            return String.format("[^%s]", keyPrefix)
        }
    }

    /**
     * Evaluator for attribute name/value matching
     */
    class AttributeWithValue constructor(key: String?, value: String?) : AttributeKeyPair(key, value) {
        public override fun matches(root: Element, element: Element): Boolean {
            return element.hasAttr(key!!) && value.equals(element.attr(key)!!.trim({ it <= ' ' }), ignoreCase = true)
        }

        public override fun toString(): String {
            return String.format("[%s=%s]", key, value)
        }
    }

    /**
     * Evaluator for attribute name != value matching
     */
    class AttributeWithValueNot constructor(key: String?, value: String?) : AttributeKeyPair(key, value) {
        public override fun matches(root: Element, element: Element): Boolean {
            return !value.equals(element.attr(key), ignoreCase = true)
        }

        public override fun toString(): String {
            return String.format("[%s!=%s]", key, value)
        }
    }

    /**
     * Evaluator for attribute name/value matching (value prefix)
     */
    class AttributeWithValueStarting constructor(key: String?, value: String?) : AttributeKeyPair(key, value, false) {
        public override fun matches(root: Element, element: Element): Boolean {
            return element.hasAttr(key!!) && Normalizer.lowerCase(element.attr(key))
                .startsWith(value!!) // value is lower case already
        }

        public override fun toString(): String {
            return String.format("[%s^=%s]", key, value)
        }
    }

    /**
     * Evaluator for attribute name/value matching (value ending)
     */
    class AttributeWithValueEnding constructor(key: String?, value: String?) : AttributeKeyPair(key, value, false) {
        public override fun matches(root: Element, element: Element): Boolean {
            return element.hasAttr(key!!) && Normalizer.lowerCase(element.attr(key))
                .endsWith(value!!) // value is lower case
        }

        public override fun toString(): String {
            return String.format("[%s$=%s]", key, value)
        }
    }

    /**
     * Evaluator for attribute name/value matching (value containing)
     */
    class AttributeWithValueContaining constructor(key: String?, value: String?) : AttributeKeyPair(key, value) {
        public override fun matches(root: Element, element: Element): Boolean {
            return element.hasAttr(key!!) && Normalizer.lowerCase(element.attr(key))
                .contains(value!!) // value is lower case
        }

        public override fun toString(): String {
            return String.format("[%s*=%s]", key, value)
        }
    }

    /**
     * Evaluator for attribute name/value matching (value regex matching)
     */
    class AttributeWithValueMatching constructor(key: String?, pattern: Regex) : Evaluator() {
        var key: String?
        var pattern: Regex

        init {
            this.key = Normalizer.normalize(key)
            this.pattern = pattern
        }

        public override fun matches(root: Element, element: Element): Boolean {
            return element.hasAttr(key!!) && pattern.containsMatchIn(element.attr(key) ?: "")
        }

        public override fun toString(): String {
            return String.format("[%s~=%s]", key, pattern.toString())
        }
    }

    /**
     * Abstract evaluator for attribute name/value matching
     */
    abstract class AttributeKeyPair constructor(key: String?, value: String?, trimValue: Boolean = true) :
        Evaluator() {
        var key: String?
        var value: String?

        init {
            var value: String? = value
            Validate.notEmpty(key)
            Validate.notEmpty(value)
            this.key = Normalizer.normalize(key)
            val isStringLiteral: Boolean = (value!!.startsWith("'") && value!!.endsWith("'")
                    || value!!.startsWith("\"") && value!!.endsWith("\""))
            if (isStringLiteral) {
                value = value!!.substring(1, value!!.length - 1)
            }
            this.value = if (trimValue) Normalizer.normalize(value) else Normalizer.normalize(value, isStringLiteral)
        }
    }

    /**
     * Evaluator for any / all element matching
     */
    class AllElements constructor() : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            return true
        }

        public override fun toString(): String {
            return "*"
        }
    }

    /**
     * Evaluator for matching by sibling index number (e &lt; idx)
     */
    class IndexLessThan constructor(index: Int) : IndexEvaluator(index) {
        public override fun matches(root: Element, element: Element): Boolean {
            return root !== element && element.elementSiblingIndex() < index
        }

        public override fun toString(): String {
            return String.format(":lt(%d)", index)
        }
    }

    /**
     * Evaluator for matching by sibling index number (e &gt; idx)
     */
    class IndexGreaterThan constructor(index: Int) : IndexEvaluator(index) {
        public override fun matches(root: Element, element: Element): Boolean {
            return element.elementSiblingIndex() > index
        }

        public override fun toString(): String {
            return String.format(":gt(%d)", index)
        }
    }

    /**
     * Evaluator for matching by sibling index number (e = idx)
     */
    class IndexEquals constructor(index: Int) : IndexEvaluator(index) {
        public override fun matches(root: Element, element: Element): Boolean {
            return element.elementSiblingIndex() == index
        }

        public override fun toString(): String {
            return String.format(":eq(%d)", index)
        }
    }

    /**
     * Evaluator for matching the last sibling (css :last-child)
     */
    class IsLastChild constructor() : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            val p: Element? = element.parent()
            return (p != null) && !(p is Document) && (element.elementSiblingIndex() == p.children().size - 1)
        }

        public override fun toString(): String {
            return ":last-child"
        }
    }

    class IsFirstOfType constructor() : IsNthOfType(0, 1) {
        public override fun toString(): String {
            return ":first-of-type"
        }
    }

    class IsLastOfType constructor() : IsNthLastOfType(0, 1) {
        public override fun toString(): String {
            return ":last-of-type"
        }
    }

    abstract class CssNthEvaluator constructor(protected val a: Int, protected val b: Int) : Evaluator() {
        constructor(b: Int) : this(0, b) {}

        public override fun matches(root: Element, element: Element): Boolean {
            val p: Element? = element.parent()
            if (p == null || (p is Document)) return false
            val pos: Int = calculatePosition(root, element)
            if (a == 0) return pos == b
            return (pos - b) * a >= 0 && (pos - b) % a == 0
        }

        public override fun toString(): String {
            if (a == 0) return String.format(":%s(%d)", pseudoClass, b)
            if (b == 0) return String.format(":%s(%dn)", pseudoClass, a)
            return String.format(":%s(%dn%+d)", pseudoClass, a, b)
        }

        protected abstract val pseudoClass: String?
        protected abstract fun calculatePosition(root: Element?, element: Element?): Int
    }

    /**
     * css-compatible Evaluator for :eq (css :nth-child)
     *
     * @see IndexEquals
     */
    class IsNthChild constructor(a: Int, b: Int) : CssNthEvaluator(a, b) {
        override fun calculatePosition(root: Element?, element: Element?): Int {
            return element!!.elementSiblingIndex() + 1
        }

        protected override val pseudoClass: String
            protected get() {
                return "nth-child"
            }
    }

    /**
     * css pseudo class :nth-last-child)
     *
     * @see IndexEquals
     */
    class IsNthLastChild constructor(a: Int, b: Int) : CssNthEvaluator(a, b) {
        override fun calculatePosition(root: Element?, element: Element?): Int {
            if (element!!.parent() == null) return 0
            return element.parent()!!.children().size - element.elementSiblingIndex()
        }

        override val pseudoClass: String?
            protected get() {
                return "nth-last-child"
            }
    }

    /**
     * css pseudo class nth-of-type
     *
     */
    open class IsNthOfType constructor(a: Int, b: Int) : CssNthEvaluator(a, b) {
        override fun calculatePosition(root: Element?, element: Element?): Int {
            var pos: Int = 0
            if (element?.parent() == null) return 0
            val family: Elements = element.parent()!!.children()
            for (el: Element in family) {
                if ((el.tag() == element.tag())) pos++
                if (el === element) break
            }
            return pos
        }

        override val pseudoClass: String?
            protected get() {
                return "nth-of-type"
            }
    }

    open class IsNthLastOfType constructor(a: Int, b: Int) : CssNthEvaluator(a, b) {
        override fun calculatePosition(root: Element?, element: Element?): Int {
            var pos: Int = 0
            if (element?.parent() == null) return 0
            val family: Elements = element.parent()!!.children()
            for (i in element.elementSiblingIndex() until family.size) {
                if ((family.get(i).tag() == element.tag())) pos++
            }
            return pos
        }

        override val pseudoClass: String?
            protected get() {
                return "nth-last-of-type"
            }
    }

    /**
     * Evaluator for matching the first sibling (css :first-child)
     */
    class IsFirstChild constructor() : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            val p: Element? = element.parent()
            return (p != null) && !(p is Document) && (element.elementSiblingIndex() == 0)
        }

        public override fun toString(): String {
            return ":first-child"
        }
    }

    /**
     * css3 pseudo-class :root
     * @see [:root selector](http://www.w3.org/TR/selectors/.root-pseudo)
     */
    class IsRoot constructor() : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            val r: Element? = if (root is Document) root.child(0) else root
            return element === r
        }

        public override fun toString(): String {
            return ":root"
        }
    }

    class IsOnlyChild constructor() : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            val p: Element? = element.parent()
            return (p != null) && p !is Document && element.siblingElements().isEmpty()
        }

        public override fun toString(): String {
            return ":only-child"
        }
    }

    class IsOnlyOfType constructor() : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            val p: Element? = element.parent()
            if (p == null || p is Document) return false
            var pos: Int = 0
            val family: Elements = p.children()
            for (el: Element in family) {
                if ((el.tag() == element.tag())) pos++
            }
            return pos == 1
        }

        public override fun toString(): String {
            return ":only-of-type"
        }
    }

    class IsEmpty constructor() : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            val family: List<Node?> = element.childNodes()
            for (n: Node? in family) {
                if (!(n is Comment || n is XmlDeclaration || n is DocumentType)) return false
            }
            return true
        }

        public override fun toString(): String {
            return ":empty"
        }
    }

    /**
     * Abstract evaluator for sibling index matching
     *
     * @author ant
     */
    abstract class IndexEvaluator constructor(var index: Int) : Evaluator()

    /**
     * Evaluator for matching Element (and its descendants) text
     */
    class ContainsText constructor(searchText: String?) : Evaluator() {
        private val searchText: String

        init {
            this.searchText = Normalizer.lowerCase(StringUtil.normaliseWhitespace(searchText))
        }

        public override fun matches(root: Element, element: Element): Boolean {
            return Normalizer.lowerCase(element.text()).contains(searchText)
        }

        public override fun toString(): String {
            return ":contains(%s)".format(searchText.toString())
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) wholeText. Neither the input nor the element text is
     * normalized. `:containsWholeText()`
     * @since 1.15.1.
     */
    class ContainsWholeText constructor(private val searchText: String?) : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            return element.wholeText().contains(searchText!!)
        }

        public override fun toString(): String {
            return ":containsWholeText(%s)".format(searchText.toString())
        }
    }

    /**
     * Evaluator for matching Element (but **not** its descendants) wholeText. Neither the input nor the element text is
     * normalized. `:containsWholeOwnText()`
     * @since 1.15.1.
     */
    class ContainsWholeOwnText constructor(private val searchText: String?) : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            return element.wholeOwnText().contains(searchText!!)
        }

        public override fun toString(): String {
            return ":containsWholeOwnText(%s)".format(searchText.toString())
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) data
     */
    class ContainsData constructor(searchText: String?) : Evaluator() {
        private val searchText: String?

        init {
            this.searchText = Normalizer.lowerCase(searchText)
        }

        public override fun matches(root: Element, element: Element): Boolean {
            return Normalizer.lowerCase(element.data()).contains(searchText!!) // not whitespace normalized
        }

        public override fun toString(): String {
            return ":containsData(%s)".format(searchText.toString())
        }
    }

    /**
     * Evaluator for matching Element's own text
     */
    class ContainsOwnText constructor(searchText: String?) : Evaluator() {
        private val searchText: String?

        init {
            this.searchText = Normalizer.lowerCase(StringUtil.normaliseWhitespace(searchText))
        }

        public override fun matches(root: Element, element: Element): Boolean {
            return Normalizer.lowerCase(element.ownText()).contains(searchText!!)
        }

        public override fun toString(): String {
            return ":containsOwn(%s)".format(searchText.toString())
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) text with regex
     */
    class Matches constructor(private val pattern: Regex) : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            return pattern.containsMatchIn(element.text())
        }

        public override fun toString(): String {
            return ":matches(%s)".format(pattern)
        }
    }

    /**
     * Evaluator for matching Element's own text with regex
     */
    class MatchesOwn constructor(private val pattern: Regex) : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            return pattern.containsMatchIn(element.ownText())
        }

        public override fun toString(): String {
            return ":matchesOwn(%s)".format(pattern)
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) whole text with regex.
     * @since 1.15.1.
     */
    class MatchesWholeText constructor(private val pattern: Regex) : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            return pattern.containsMatchIn(element.wholeText())
        }

        public override fun toString(): String {
            return ":matchesWholeText(%s)".format(pattern)
        }
    }

    /**
     * Evaluator for matching Element's own whole text with regex.
     * @since 1.15.1.
     */
    class MatchesWholeOwnText constructor(private val pattern: Regex) : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            return pattern.containsMatchIn(element.wholeOwnText())
        }

        public override fun toString(): String {
            return ":matchesWholeOwnText(%s)".format(pattern)
        }
    }

    class MatchText constructor() : Evaluator() {
        public override fun matches(root: Element, element: Element): Boolean {
            if (element is PseudoTextElement) return true
            val textNodes: List<TextNode?> = element.textNodes()
            for (textNode: TextNode? in textNodes) {
                val pel: PseudoTextElement = PseudoTextElement(
                    valueOf(element.tagName()), element.baseUri(), element.attributes()
                )
                textNode?.replaceWith(pel)
                pel.appendChild(textNode!!)
            }
            return false
        }

        public override fun toString(): String {
            return ":matchText"
        }
    }
}
