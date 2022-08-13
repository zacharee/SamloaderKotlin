package org.jsoup.select

import com.soywiz.korio.lang.format
import org.jsoup.helper.Validate
import org.jsoup.nodes.Element

/**
 * CSS-like element selector, that finds elements matching a query.
 *
 * <h2>Selector syntax</h2>
 *
 *
 * A selector is a chain of simple selectors, separated by combinators. Selectors are **case insensitive** (including against
 * elements, attributes, and attribute values).
 *
 *
 *
 * The universal selector (*) is implicit when no element selector is supplied (i.e. `*.header` and `.header`
 * is equivalent).
 *
 * <style></style>
 * <table summary="" class="syntax"><colgroup><col span="1" style="width: 20%;"></col><col span="1" style="width: 40%;"></col><col span="1" style="width: 40%;"></col></colgroup>
 * <tr><th align="left">Pattern</th><th align="left">Matches</th><th align="left">Example</th></tr>
 * <tr><td>`*`</td><td>any element</td><td>`*`</td></tr>
 * <tr><td>`tag`</td><td>elements with the given tag name</td><td>`div`</td></tr>
 * <tr><td>`*|E`</td><td>elements of type E in any namespace (including non-namespaced)</td><td>`*|name` finds `<fb:name>` and `<name>` elements</td></tr>
 * <tr><td>`ns|E`</td><td>elements of type E in the namespace *ns*</td><td>`fb|name` finds `<fb:name>` elements</td></tr>
 * <tr><td>`#id`</td><td>elements with attribute ID of "id"</td><td>`div#wrap`, `#logo`</td></tr>
 * <tr><td>`.class`</td><td>elements with a class name of "class"</td><td>`div.left`, `.result`</td></tr>
 * <tr><td>`[attr]`</td><td>elements with an attribute named "attr" (with any value)</td><td>`a[href]`, `[title]`</td></tr>
 * <tr><td>`[^attrPrefix]`</td><td>elements with an attribute name starting with "attrPrefix". Use to find elements with HTML5 datasets</td><td>`[^data-]`, `div[^data-]`</td></tr>
 * <tr><td>`[attr=val]`</td><td>elements with an attribute named "attr", and value equal to "val"</td><td>`img[width=500]`, `a[rel=nofollow]`</td></tr>
 * <tr><td>`[attr="val"]`</td><td>elements with an attribute named "attr", and value equal to "val"</td><td>`span[hello="Cleveland"][goodbye="Columbus"]`, `a[rel="nofollow"]`</td></tr>
 * <tr><td>`[attr^=valPrefix]`</td><td>elements with an attribute named "attr", and value starting with "valPrefix"</td><td>`a[href^=http:]`</td></tr>
 * <tr><td>`[attr$=valSuffix]`</td><td>elements with an attribute named "attr", and value ending with "valSuffix"</td><td>`img[src$=.png]`</td></tr>
 * <tr><td>`[attr*=valContaining]`</td><td>elements with an attribute named "attr", and value containing "valContaining"</td><td>`a[href*=/search/]`</td></tr>
 * <tr><td>`[attr~=*regex*]`</td><td>elements with an attribute named "attr", and value matching the regular expression</td><td>`img[src~=(?i)\\.(png|jpe?g)]`</td></tr>
 * <tr><td></td><td>The above may be combined in any order</td><td>`div.header[title]`</td></tr>
 * <tr><td></td><td colspan="3"><h3>Combinators</h3></td></tr>
 * <tr><td>`E F`</td><td>an F element descended from an E element</td><td>`div a`, `.logo h1`</td></tr>
 * <tr><td>`E > F`</td><td>an F direct child of E</td><td>`ol > li`</td></tr>
 * <tr><td>`E + F`</td><td>an F element immediately preceded by sibling E</td><td>`li + li`, `div.head + div`</td></tr>
 * <tr><td>`E ~ F`</td><td>an F element preceded by sibling E</td><td>`h1 ~ p`</td></tr>
 * <tr><td>`E, F, G`</td><td>all matching elements E, F, or G</td><td>`a[href], div, h3`</td></tr>
 * <tr><td></td><td colspan="3"><h3>Pseudo selectors</h3></td></tr>
 * <tr><td>`:lt(*n*)`</td><td>elements whose sibling index is less than *n*</td><td>`td:lt(3)` finds the first 3 cells of each row</td></tr>
 * <tr><td>`:gt(*n*)`</td><td>elements whose sibling index is greater than *n*</td><td>`td:gt(1)` finds cells after skipping the first two</td></tr>
 * <tr><td>`:eq(*n*)`</td><td>elements whose sibling index is equal to *n*</td><td>`td:eq(0)` finds the first cell of each row</td></tr>
 * <tr><td>`:has(*selector*)`</td><td>elements that contains at least one element matching the *selector*</td><td>`div:has(p)` finds `div`s that contain `p` elements.<br></br>`div:has(> a)` selects `div` elements that have at least one direct child `a` element.</td></tr>
 * <tr><td>`:not(*selector*)`</td><td>elements that do not match the *selector*. See also [Elements.not]</td><td>`div:not(.logo)` finds all divs that do not have the "logo" class.
 *
 *`div:not(:has(div))` finds divs that do not contain divs.</td></tr>
 * <tr><td>`:contains(*text*)`</td><td>elements that contains the specified text. The search is case insensitive. The text may appear in the found element, or any of its descendants. The text is whitespace normalized.
 *
 *To find content that includes parentheses, escape those with a `\`.</td><td>`p:contains(jsoup)` finds p elements containing the text "jsoup".
 *
 *`p:contains(hello \(there\) finds p elements containing the text "Hello (There)"`</td></tr>
 * <tr><td>`:containsOwn(*text*)`</td><td>elements that directly contain the specified text. The search is case insensitive. The text must appear in the found element, not any of its descendants.</td><td>`p:containsOwn(jsoup)` finds p elements with own text "jsoup".</td></tr>
 * <tr><td>`:containsData(*data*)`</td><td>elements that contains the specified *data*. The contents of `script` and `style` elements, and `comment` nodes (etc) are considered data nodes, not text nodes. The search is case insensitive. The data may appear in the found element, or any of its descendants.</td><td>`script:contains(jsoup)` finds script elements containing the data "jsoup".</td></tr>
 * <tr><td>`:containsWholeText(*text*)`</td><td>elements that contains the specified **non-normalized** text. The search is case sensitive, and will match exactly against spaces and newlines found in the original input. The text may appear in the found element, or any of its descendants.
 *
 *To find content that includes parentheses, escape those with a `\`.</td><td>`p:containsWholeText(jsoup\nThe Java HTML Parser)` finds p elements containing the text `"jsoup\nThe Java HTML Parser"` (and not other variations of whitespace or casing, as `:contains()` would. Note that `br` elements are presented as a newline.</td></tr>
 * <tr><td>`:containsWholeOwnText(*text*)`</td><td>elements that **directly** contain the specified **non-normalized** text. The search is case sensitive, and will match exactly against spaces and newlines found in the original input. The text may appear in the found element, but not in its descendants.
 *
 *To find content that includes parentheses, escape those with a `\`.</td><td>`p:containsWholeOwnText(jsoup\nThe Java HTML Parser)` finds p elements directly containing the text `"jsoup\nThe Java HTML Parser"` (and not other variations of whitespace or casing, as `:contains()` would. Note that `br` elements are presented as a newline.</td></tr>
 * <tr><td>`:matches(*regex*)`</td><td>elements containing **whitespace normalized** text that matches the specified regular expression. The text may appear in the found element, or any of its descendants.</td><td>`td:matches(\\d+)` finds table cells containing digits. `div:matches((?i)login)` finds divs containing the text, case insensitively.</td></tr>
 * <tr><td>`:matchesWholeText(*regex*)`</td><td>elements containing **non-normalized** whole text that matches the specified regular expression. The text may appear in the found element, or any of its descendants.</td><td>`td:matchesWholeText(\\s{2,})` finds table cells a run of at least two space characters.</td></tr>
 * <tr><td>`:matchesWholeOwnText(*regex*)`</td><td>elements whose own **non-normalized** whole text matches the specified regular expression. The text must appear in the found element, not any of its descendants.</td><td>`td:matchesWholeOwnText(\n\\d+)` finds table cells directly containing digits following a neewline.</td></tr>
 * <tr><td></td><td>The above may be combined in any order and with other selectors</td><td>`.light:contains(name):eq(0)`</td></tr>
 * <tr><td>`:matchText`</td><td>treats text nodes as elements, and so allows you to match against and select text nodes.
 *
 ***Note** that using this selector will modify the DOM, so you may want to `clone` your document before using.</td><td>`p:matchText:firstChild` with input `<p>One<br />Two</p>` will return one [org.jsoup.nodes.PseudoTextElement] with text "`One`".</td></tr>
 * <tr><td colspan="3"><h3>Structural pseudo selectors</h3></td></tr>
 * <tr><td>`:root`</td><td>The element that is the root of the document. In HTML, this is the `html` element</td><td>`:root`</td></tr>
 * <tr><td>`:nth-child(*a*n+*b*)`</td><td>
 *
 *elements that have `*a*n+*b*-1` siblings **before** it in the document tree, for any positive integer or zero value of `n`, and has a parent element. For values of `a` and `b` greater than zero, this effectively divides the element's children into groups of a elements (the last group taking the remainder), and selecting the *b*th element of each group. For example, this allows the selectors to address every other row in a table, and could be used to alternate the color of paragraph text in a cycle of four. The `a` and `b` values must be integers (positive, negative, or zero). The index of the first child of an element is 1.
 * In addition to this, `:nth-child()` can take `odd` and `even` as arguments instead. `odd` has the same signification as `2n+1`, and `even` has the same signification as `2n`.</td><td>`tr:nth-child(2n+1)` finds every odd row of a table. `:nth-child(10n-1)` the 9th, 19th, 29th, etc, element. `li:nth-child(5)` the 5h li</td></tr>
 * <tr><td>`:nth-last-child(*a*n+*b*)`</td><td>elements that have `*a*n+*b*-1` siblings **after** it in the document tree. Otherwise like `:nth-child()`</td><td>`tr:nth-last-child(-n+2)` the last two rows of a table</td></tr>
 * <tr><td>`:nth-of-type(*a*n+*b*)`</td><td>pseudo-class notation represents an element that has `*a*n+*b*-1` siblings with the same expanded element name *before* it in the document tree, for any zero or positive integer value of n, and has a parent element</td><td>`img:nth-of-type(2n+1)`</td></tr>
 * <tr><td>`:nth-last-of-type(*a*n+*b*)`</td><td>pseudo-class notation represents an element that has `*a*n+*b*-1` siblings with the same expanded element name *after* it in the document tree, for any zero or positive integer value of n, and has a parent element</td><td>`img:nth-last-of-type(2n+1)`</td></tr>
 * <tr><td>`:first-child`</td><td>elements that are the first child of some other element.</td><td>`div > p:first-child`</td></tr>
 * <tr><td>`:last-child`</td><td>elements that are the last child of some other element.</td><td>`ol > li:last-child`</td></tr>
 * <tr><td>`:first-of-type`</td><td>elements that are the first sibling of its type in the list of children of its parent element</td><td>`dl dt:first-of-type`</td></tr>
 * <tr><td>`:last-of-type`</td><td>elements that are the last sibling of its type in the list of children of its parent element</td><td>`tr > td:last-of-type`</td></tr>
 * <tr><td>`:only-child`</td><td>elements that have a parent element and whose parent element have no other element children</td><td></td></tr>
 * <tr><td>`:only-of-type`</td><td> an element that has a parent element and whose parent element has no other element children with the same expanded element name</td><td></td></tr>
 * <tr><td>`:empty`</td><td>elements that have no children at all</td><td></td></tr>
</table> *
 *
 *
 * A word on using regular expressions in these selectors: depending on the content of the regex, you will need to quote the pattern using **`Pattern.quote("regex")`** for it to parse correclty through both the selector parser and the regex parser. E.g. `String query = "div:matches(" + Pattern.quote(regex) + ");"`.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 * @see Element.select
 */
object Selector {
    /**
     * Find elements matching selector.
     *
     * @param query CSS selector
     * @param root  root element to descend into
     * @return matching elements, empty if none
     * @throws SelectorParseException (unchecked) on an invalid CSS query.
     */
    fun select(query: String, root: Element): Elements {
        Validate.notEmpty(query)
        return select(QueryParser.parse(query), root)
    }

    /**
     * Find elements matching selector.
     *
     * @param evaluator CSS selector
     * @param root root element to descend into
     * @return matching elements, empty if none
     */
    fun select(evaluator: Evaluator, root: Element): Elements {
        Validate.notNull(evaluator)
        Validate.notNull(root)
        return Collector.collect(evaluator, root)
    }

    /**
     * Find elements matching selector.
     *
     * @param query CSS selector
     * @param roots root elements to descend into
     * @return matching elements, empty if none
     */
    fun select(query: String, roots: Iterable<Element>): Elements {
        Validate.notEmpty(query)
        Validate.notNull(roots)
        val evaluator: Evaluator = QueryParser.parse(query)
        val elements = Elements()
        val seenElements: HashMap<Element, Element?> = HashMap()
        // dedupe elements by identity, not equality
        for (root: Element in roots) {
            val found: Elements = select(evaluator, root)
            for (el: Element in found) {
                if (seenElements[el] === el || seenElements.put(el, el) == null) {
                    elements.add(el)
                }
            }
        }
        return elements
    }

    // exclude set. package open so that Elements can implement .not() selector.
    fun filterOut(elements: Collection<Element>, outs: Collection<Element>?): Elements {
        val output = Elements()
        for (el: Element in elements) {
            var found = false
            for (out: Element in outs!!) {
                if ((el == out)) {
                    found = true
                    break
                }
            }
            if (!found) output.add(el)
        }
        return output
    }

    /**
     * Find the first element that matches the query.
     * @param cssQuery CSS selector
     * @param root root element to descend into
     * @return the matching element, or **null** if none.
     */
    fun selectFirst(cssQuery: String, root: Element): Element? {
        Validate.notEmpty(cssQuery)
        return Collector.findFirst(QueryParser.parse(cssQuery), root)
    }

    class SelectorParseException : IllegalStateException {
        constructor(msg: String?) : super(msg)
        constructor(msg: String?, vararg params: Any?) : super(msg?.format(params))
    }
}
