package org.jsoup.safety

import org.jsoup.helper.Validate
import org.jsoup.nodes.*
import org.jsoup.parser.ParseErrorList
import org.jsoup.parser.Parser
import org.jsoup.parser.Tag.Companion.valueOf
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor

/**
 * The safelist based HTML cleaner. Use to ensure that end-user provided HTML contains only the elements and attributes
 * that you are expecting; no junk, and no cross-site scripting attacks!
 *
 *
 * The HTML cleaner parses the input as HTML and then runs it through a safe-list, so the output HTML can only contain
 * HTML that is allowed by the safelist.
 *
 *
 *
 * It is assumed that the input HTML is a body fragment; the clean methods only pull from the source's body, and the
 * canned safe-lists only allow body contained tags.
 *
 *
 *
 * Rather than interacting directly with a Cleaner object, generally see the `clean` methods in [org.jsoup.Jsoup].
 *
 */
class Cleaner constructor(private val safelist: Safelist) {

    /**
     * Creates a new, clean document, from the original dirty document, containing only elements allowed by the safelist.
     * The original document is not modified. Only elements from the dirty document's `body` are used. The
     * OutputSettings of the original document are cloned into the clean document.
     * @param dirtyDocument Untrusted base document to clean.
     * @return cleaned document.
     */
    fun clean(dirtyDocument: Document): Document {
        Validate.notNull(dirtyDocument)
        val clean: Document = Document.createShell(dirtyDocument.baseUri())
        copySafeNodes(dirtyDocument.body(), clean.body())
        clean.outputSettings(dirtyDocument.outputSettings().clone())
        return clean
    }

    /**
     * Determines if the input document **body**is valid, against the safelist. It is considered valid if all the tags and attributes
     * in the input HTML are allowed by the safelist, and that there is no content in the `head`.
     *
     *
     * This method can be used as a validator for user input. An invalid document will still be cleaned successfully
     * using the [.clean] document. If using as a validator, it is recommended to still clean the document
     * to ensure enforced attributes are set correctly, and that the output is tidied.
     *
     * @param dirtyDocument document to test
     * @return true if no tags or attributes need to be removed; false if they do
     */
    fun isValid(dirtyDocument: Document): Boolean {
        Validate.notNull(dirtyDocument)
        val clean: Document = Document.createShell(dirtyDocument.baseUri())
        val numDiscarded: Int = copySafeNodes(dirtyDocument.body(), clean.body())
        return (numDiscarded == 0 && dirtyDocument.head().childNodes().isEmpty()) // because we only look at the body, but we start from a shell, make sure there's nothing in the head
    }

    fun isValidBodyHtml(bodyHtml: String): Boolean {
        val clean: Document = Document.createShell("")
        val dirty: Document = Document.createShell("")
        val errorList: ParseErrorList = ParseErrorList.tracking(1)
        val nodes: List<Node> = Parser.parseFragment(bodyHtml, dirty.body(), "", errorList)
        dirty.body().insertChildren(0, nodes)
        val numDiscarded: Int = copySafeNodes(dirty.body(), clean.body())
        return numDiscarded == 0 && errorList.isEmpty()
    }

    /**
     * Iterates the input and copies trusted nodes (tags, attributes, text) into the destination.
     */
    private inner class CleaningVisitor constructor(private val root: Element?, destination: Element?) :
        NodeVisitor {
        var numDiscarded: Int = 0
        private var destination // current element to append nodes to
                : Element?

        init {
            this.destination = destination
        }

        override fun head(node: Node, depth: Int) {
            if (node is Element) {
                val sourceEl: Element = node
                if (safelist.isSafeTag(sourceEl.normalName())) { // safe, clone and copy safe attrs
                    val meta: ElementMeta = createSafeElement(sourceEl)
                    val destChild: Element = meta.el
                    destination?.appendChild(destChild)
                    numDiscarded += meta.numAttribsDiscarded
                    destination = destChild
                } else if (node !== root) { // not a safe tag, so don't add. don't count root against discarded.
                    numDiscarded++
                }
            } else if (node is TextNode) {
                val sourceText: TextNode = node
                val destText = TextNode(sourceText.wholeText)
                destination?.appendChild(destText)
            } else if (node is DataNode && safelist.isSafeTag(node.parent()!!.nodeName())) {
                val sourceData: DataNode = node
                val destData = DataNode(sourceData.wholeData)
                destination?.appendChild(destData)
            } else { // else, we don't care about comments, xml proc instructions, etc
                numDiscarded++
            }
        }

        override fun tail(node: Node?, depth: Int) {
            if (node is Element && safelist.isSafeTag(node.nodeName())) {
                destination = destination?.parent() // would have descended, so pop destination stack
            }
        }
    }

    private fun copySafeNodes(source: Element?, dest: Element?): Int {
        val cleaningVisitor = CleaningVisitor(source, dest)
        NodeTraversor.traverse(cleaningVisitor, source)
        return cleaningVisitor.numDiscarded
    }

    private fun createSafeElement(sourceEl: Element): ElementMeta {
        val sourceTag: String = sourceEl.tagName()
        val destAttrs = Attributes()
        val dest = Element(valueOf(sourceTag), sourceEl.baseUri(), destAttrs)
        var numDiscarded = 0
        val sourceAttrs: Attributes = sourceEl.attributes()
        sourceAttrs.forEach { sourceAttr ->
            if (safelist.isSafeAttribute(sourceTag, sourceEl, sourceAttr!!)) destAttrs.put(sourceAttr) else numDiscarded++
        }
        val enforcedAttrs: Attributes = safelist.getEnforcedAttributes(sourceTag)
        destAttrs.addAll(enforcedAttrs)

        // Copy the original start and end range, if set
        // TODO - might be good to make a generic Element#userData set type interface, and copy those all over
        if (sourceEl.sourceRange().isTracked) sourceEl.sourceRange().track(dest, true)
        if (sourceEl.endSourceRange().isTracked) sourceEl.endSourceRange().track(dest, false)
        return ElementMeta(dest, numDiscarded)
    }

    private class ElementMeta(var el: Element, var numAttribsDiscarded: Int)
}
