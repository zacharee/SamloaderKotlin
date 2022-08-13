package org.jsoup.nodes

import org.jsoup.parser.Tag

/**
 * Represents a [TextNode] as an [Element], to enable text nodes to be selected with
 * the [org.jsoup.select.Selector] `:matchText` syntax.
 */
class PseudoTextElement constructor(tag: Tag, baseUri: String?, attributes: Attributes?) :
    Element(tag, baseUri, attributes) {
    override fun outerHtmlHead(accum: Appendable?, depth: Int, out: Document.OutputSettings?) {}
    override fun outerHtmlTail(accum: Appendable?, depth: Int, out: Document.OutputSettings?) {}
}
