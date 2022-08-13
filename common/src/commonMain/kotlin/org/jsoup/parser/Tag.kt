package org.jsoup.parser

import org.jsoup.helper.Validate
import org.jsoup.internal.Normalizer

/**
 * HTML Tag capabilities.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
data class Tag private constructor(
    /**
     * Get this tag's name.
     *
     * @return the tag's name
     */
    var name: String,
    var isBlock: Boolean = true,
    private var formatAsBlock: Boolean = true,
    var isEmpty: Boolean = true,
    private var selfClosing: Boolean = false,
    private var preserveWhitespace: Boolean = false,
    var isFormListed: Boolean = false, // a control that appears in forms: input, textarea, output etc
    var isFormSubmittable: Boolean = false // a control that can be submitted in a form: input etc
) {
    private val normalName // always the lower case version of this tag, regardless of case preservation mode
            : String = Normalizer.lowerCase(name)!!

    /**
     * Get this tag's normalized (lowercased) name.
     * @return the tag's normal name.
     */
    fun normalName(): String {
        return normalName
    }

    /**
     * Gets if this tag should be formatted as a block (or as inline)
     *
     * @return if should be formatted as block or inline
     */
    fun formatAsBlock(): Boolean {
        return formatAsBlock
    }

    /**
     * Gets if this tag is an inline tag.
     *
     * @return if this tag is an inline tag.
     */
    val isInline: Boolean
        get() {
            return !isBlock
        }

    /**
     * Get if this tag is self closing.
     *
     * @return if this tag should be output as self closing.
     */
    fun isSelfClosing(): Boolean {
        return isEmpty || selfClosing
    }

    /**
     * Get if this is a pre-defined tag, or was auto created on parsing.
     *
     * @return if a known tag
     */
    val isKnownTag: Boolean
        get() {
            return tags.containsKey(name)
        }

    /**
     * Get if this tag should preserve whitespace within child text nodes.
     *
     * @return if preserve whitespace
     */
    fun preserveWhitespace(): Boolean {
        return preserveWhitespace
    }

    fun setSelfClosing(): Tag {
        selfClosing = true
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tag) return false
        val tag: Tag = other
        if (name != tag.name) return false
        if (isEmpty != tag.isEmpty) return false
        if (formatAsBlock != tag.formatAsBlock) return false
        if (isBlock != tag.isBlock) return false
        if (preserveWhitespace != tag.preserveWhitespace) return false
        if (selfClosing != tag.selfClosing) return false
        if (isFormListed != tag.isFormListed) return false
        return isFormSubmittable == tag.isFormSubmittable
    }

    override fun hashCode(): Int {
        var result: Int = name.hashCode()
        result = 31 * result + (if (isBlock) 1 else 0)
        result = 31 * result + (if (formatAsBlock) 1 else 0)
        result = 31 * result + (if (isEmpty) 1 else 0)
        result = 31 * result + (if (selfClosing) 1 else 0)
        result = 31 * result + (if (preserveWhitespace) 1 else 0)
        result = 31 * result + (if (isFormListed) 1 else 0)
        result = 31 * result + (if (isFormSubmittable) 1 else 0)
        return result
    }

    override fun toString(): String {
        return name
    }

    companion object {
        private val tags: MutableMap<String?, Tag> = HashMap() // map of known tags
        /**
         * Get a Tag by name. If not previously defined (unknown), returns a new generic tag, that can do anything.
         *
         *
         * Pre-defined tags (P, DIV etc) will be ==, but unknown tags are not registered and will only .equals().
         *
         *
         * @param tagName Name of tag, e.g. "p". Case insensitive.
         * @param settings used to control tag name sensitivity
         * @return The tag, either defined or new generic.
         */
        /**
         * Get a Tag by name. If not previously defined (unknown), returns a new generic tag, that can do anything.
         *
         *
         * Pre-defined tags (P, DIV etc) will be ==, but unknown tags are not registered and will only .equals().
         *
         *
         * @param tagName Name of tag, e.g. "p". **Case sensitive**.
         * @return The tag, either defined or new generic.
         */
        fun valueOf(tagName: String, settings: ParseSettings? = ParseSettings.preserveCase): Tag {
            var tagName: String = tagName
            Validate.notNull(tagName)
            var tag: Tag? = tags[tagName]
            if (tag == null) {
                tagName = settings!!.normalizeTag(tagName)!! // the name we'll use
                Validate.notEmpty(tagName)
                val normalName = Normalizer.lowerCase(tagName) // the lower-case name to get tag settings off
                tag = tags[normalName]
                if (tag == null) {
                    // not defined: create default; go anywhere, do anything! (incl be inside a <p>)
                    tag = Tag(tagName)
                    tag.isBlock = false
                } else if (settings.preserveTagCase() && tagName != normalName) {
                    tag = tag.copy() // get a new version vs the static one, so name update doesn't reset all
                    tag.name = (tagName)
                }
            }
            return tag
        }

        /**
         * Check if this tagname is a known tag.
         *
         * @param tagName name of tag
         * @return if known HTML tag
         */
        fun isKnownTag(tagName: String): Boolean {
            return tags.containsKey(tagName)
        }

        // internal static initialisers:
        // prepped from http://www.w3.org/TR/REC-html40/sgml/dtd.html and other sources
        private val blockTags: Array<String> = arrayOf(
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
            "math",
            "center",
            "template",
            "dir",
            "applet",
            "marquee",
            "listing" // deprecated but still known / special handling
        )
        private val inlineTags: Array<String> = arrayOf(
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
            "bdi",
            "s",
            "strike",
            "nobr"
        )
        private val emptyTags: Array<String> = arrayOf(
            "meta", "link", "base", "frame", "img", "br", "wbr", "embed", "hr", "input", "keygen", "col", "command",
            "device", "area", "basefont", "bgsound", "menuitem", "param", "source", "track"
        )

        // todo - rework this to format contents as inline; and update html emitter in Element. Same output, just neater.
        private val formatAsInlineTags: Array<String> = arrayOf(
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
        private val preserveWhitespaceTags: Array<String> = arrayOf(
            "pre",
            "plaintext",
            "title",
            "textarea" // script is not here as it is a data node, which always preserve whitespace
        )

        // todo: I think we just need submit tags, and can scrub listed
        private val formListedTags: Array<String> = arrayOf(
            "button", "fieldset", "input", "keygen", "object", "output", "select", "textarea"
        )
        private val formSubmitTags: Array<String> = arrayOf(
            "input", "keygen", "object", "select", "textarea"
        )

        init {
            // creates
            for (tagName: String in blockTags) {
                val tag = Tag(tagName)
                register(tag)
            }
            for (tagName: String in inlineTags) {
                val tag = Tag(tagName)
                tag.isBlock = false
                tag.formatAsBlock = false
                register(tag)
            }

            // mods:
            for (tagName: String? in emptyTags) {
                val tag: Tag? = tags[tagName]
                Validate.notNull(tag)
                tag?.isEmpty = true
            }
            for (tagName: String? in formatAsInlineTags) {
                val tag: Tag? = tags[tagName]
                Validate.notNull(tag)
                tag?.formatAsBlock = false
            }
            for (tagName: String? in preserveWhitespaceTags) {
                val tag: Tag? = tags[tagName]
                Validate.notNull(tag)
                tag?.preserveWhitespace = true
            }
            for (tagName: String? in formListedTags) {
                val tag: Tag? = tags[tagName]
                Validate.notNull(tag)
                tag?.isFormListed = true
            }
            for (tagName: String? in formSubmitTags) {
                val tag: Tag? = tags[tagName]
                Validate.notNull(tag)
                tag?.isFormSubmittable = true
            }
        }

        private fun register(tag: Tag) {
            tags[tag.name] = tag
        }
    }
}
