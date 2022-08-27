package swiftsoup

import com.soywiz.korio.lang.assert

open class Tag(var tagName: String) {
    companion object {
        val tags = initializeMaps()

        fun valueOf(tagName: String, settings: ParseSettings): Tag {
            var tagName = tagName
            var tag: Tag? = tags[tagName]

            if (tag == null) {
                tagName = settings.normalizeTag(tagName)
                assert(tagName.isNotEmpty())
                tag = tags[tagName]

                if (tag == null) {
                    // not defined: create default; go anywhere, do anything! (incl be inside a <p>)
                    tag = Tag(tagName)
                    tag.isBlock = false
                    tag.canContainBlock = true
                }
            }
            return tag
        }

        fun valueOf(tagName: String): Tag {
            return valueOf(tagName, ParseSettings.preserveCase)
        }

        private fun initializeMaps(): Map<String, Tag> {
            val dict = HashMap<String, Tag>()

            // creates
            for (tagName in blockTags) {
                val tag = Tag(tagName)
                dict[tag.tagName] = tag
            }
            for (tagName in inlineTags) {
                val tag = Tag(tagName)
                tag.isBlock = false
                tag.canContainBlock = false
                tag.formatAsBlock = false
                dict[tag.tagName] = tag
            }

            // mods:
            for (tagName in emptyTags) {
                val tag = dict[tagName]
                tag?.canContainBlock = false
                tag?.canContainInline = false
                tag?.empty = true
            }

            for (tagName in formatAsInlineTags) {
                val tag = dict[tagName]
                tag?.formatAsBlock = false
            }

            for (tagName in preserveWhitespaceTags) {
                val tag = dict[tagName]
                tag?.preserveWhitespace = true
            }

            for (tagName in formListedTags) {
                val tag = dict[tagName]
                tag?.formList = true
            }

            for (tagName in formSubmitTags) {
                val tag = dict[tagName]
                tag?.formSubmit = true
            }

            return dict
        }

        private val blockTags = arrayOf(
            "html",
            "head",
            "body",
            "frameset",
            "script",
            "noscript",
            "style",
            "meta",
            "link",
            "title",
            "frame",
            "noframes",
            "section",
            "nav",
            "aside",
            "hgroup",
            "header",
            "footer",
            "p",
            "h1",
            "h2",
            "h3",
            "h4",
            "h5",
            "h6",
            "ul",
            "ol",
            "pre",
            "div",
            "blockquote",
            "hr",
            "address",
            "figure",
            "figcaption",
            "form",
            "fieldset",
            "ins",
            "del",
            "s",
            "dl",
            "dt",
            "dd",
            "li",
            "table",
            "caption",
            "thead",
            "tfoot",
            "tbody",
            "colgroup",
            "col",
            "tr",
            "th",
            "td",
            "video",
            "audio",
            "canvas",
            "details",
            "menu",
            "plaintext",
            "template",
            "article",
            "main",
            "svg",
            "math"
        )
        private val inlineTags = arrayOf(
            "object",
            "base",
            "font",
            "tt",
            "i",
            "b",
            "u",
            "big",
            "small",
            "em",
            "strong",
            "dfn",
            "code",
            "samp",
            "kbd",
            "var",
            "cite",
            "abbr",
            "time",
            "acronym",
            "mark",
            "ruby",
            "rt",
            "rp",
            "a",
            "img",
            "br",
            "wbr",
            "map",
            "q",
            "sub",
            "sup",
            "bdo",
            "iframe",
            "embed",
            "span",
            "input",
            "select",
            "textarea",
            "label",
            "button",
            "optgroup",
            "option",
            "legend",
            "datalist",
            "keygen",
            "output",
            "progress",
            "meter",
            "area",
            "param",
            "source",
            "track",
            "summary",
            "command",
            "device",
            "area",
            "basefont",
            "bgsound",
            "menuitem",
            "param",
            "source",
            "track",
            "data",
            "bdi"
        )
        private val emptyTags = arrayOf(
            "meta", "link", "base", "frame", "img", "br", "wbr", "embed", "hr", "input", "keygen", "col", "command",
            "device", "area", "basefont", "bgsound", "menuitem", "param", "source", "track"
        )
        private val formatAsInlineTags = arrayOf(
            "title",
            "a",
            "p",
            "h1",
            "h2",
            "h3",
            "h4",
            "h5",
            "h6",
            "pre",
            "address",
            "li",
            "th",
            "td",
            "script",
            "style",
            "ins",
            "del",
            "s"
        )
        private val preserveWhitespaceTags = arrayOf(
            "pre", "plaintext", "title", "textarea"
            // script is not here as it is a data node, which always preserve whitespace
        )

        // todo: I think we just need submit tags, and can scrub listed
        private val formListedTags = arrayOf(
            "button", "fieldset", "input", "keygen", "object", "output", "select", "textarea"
        )
        private val formSubmitTags = arrayOf(
            "input", "keygen", "object", "select", "textarea"
        )
    }

    var tagNameNormal: String = tagName.lowercase()
    var isBlock: Boolean = true // block or inline
    var formatAsBlock: Boolean = true // should be formatted as a block
    var canContainBlock: Boolean = true // Can this tag hold block level tags?
    var canContainInline: Boolean = true // only pcdata if not
    var empty: Boolean = false // can hold nothing e.g. img
    var selfClosing: Boolean =
        false // can self close (<foo />). used for unknown tags that self close, without forcing them as empty.
    var preserveWhitespace: Boolean = false // for pre, textarea, script etc
    var formList: Boolean = false // a control that appears in forms: input, textarea, output etc
    var formSubmit: Boolean = false // a control that can be submitted in a form: input etc

    val isInline: Boolean
        get() = !isBlock

    val isData: Boolean
        get() = !canContainBlock && !empty

    val isSelfClosing: Boolean
        get() = empty || selfClosing

    val isKnownTag: Boolean
        get() = tags[tagName] != null

    fun isKnownTag(tagName: String): Boolean = tags[tagName] != null


}
