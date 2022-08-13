package org.jsoup.nodes

import io.ktor.utils.io.errors.*
import org.jsoup.helper.Validate
import org.jsoup.internal.StringUtil

/**
 * A `<!DOCTYPE>` node.
 */
class DocumentType(name: String?, publicId: String?, systemId: String?) : LeafNode() {
    // todo: quirk mode from publicId and systemId
    /**
     * Create a new doctype element.
     * @param name the doctype's name
     * @param publicId the doctype's public ID
     * @param systemId the doctype's system ID
     */
    init {
        Validate.notNull(name)
        Validate.notNull(publicId)
        Validate.notNull(systemId)
        attr(NAME, name)
        attr(PUBLIC_ID, publicId)
        attr(SYSTEM_ID, systemId)
        updatePubSyskey()
    }

    override fun newInstance(): Node {
        return DocumentType(name(), publicId(), systemId())
    }

    fun setPubSysKey(value: String?) {
        if (value != null) attr(PUB_SYS_KEY, value)
    }

    private fun updatePubSyskey() {
        if (has(PUBLIC_ID)) {
            attr(PUB_SYS_KEY, PUBLIC_KEY)
        } else if (has(SYSTEM_ID)) attr(PUB_SYS_KEY, SYSTEM_KEY)
    }

    /**
     * Get this doctype's name (when set, or empty string)
     * @return doctype name
     */
    fun name(): String {
        return attr(NAME)
    }

    /**
     * Get this doctype's Public ID (when set, or empty string)
     * @return doctype Public ID
     */
    fun publicId(): String {
        return attr(PUBLIC_ID)
    }

    /**
     * Get this doctype's System ID (when set, or empty string)
     * @return doctype System ID
     */
    fun systemId(): String {
        return attr(SYSTEM_ID)
    }

    override fun nodeName(): String {
        return "#doctype"
    }

    @Throws(IOException::class)
    override fun outerHtmlHead(accum: Appendable, depth: Int, out: Document.OutputSettings) {
        // add a newline if the doctype has a preceding node (which must be a comment)
        if (sibIndex > 0 && out.prettyPrint()) accum.append('\n')
        if (out.syntax() == Document.OutputSettings.Syntax.html && !has(PUBLIC_ID) && !has(SYSTEM_ID)) {
            // looks like a html5 doctype, go lowercase for aesthetics
            accum.append("<!doctype")
        } else {
            accum.append("<!DOCTYPE")
        }
        if (has(NAME)) accum.append(" ").append(attr(NAME))
        if (has(PUB_SYS_KEY)) accum.append(" ").append(attr(PUB_SYS_KEY))
        if (has(PUBLIC_ID)) accum.append(" \"").append(attr(PUBLIC_ID)).append('"')
        if (has(SYSTEM_ID)) accum.append(" \"").append(attr(SYSTEM_ID)).append('"')
        accum.append('>')
    }

    override fun outerHtmlTail(accum: Appendable, depth: Int, out: Document.OutputSettings) {}

    private fun has(attribute: String): Boolean {
        return !StringUtil.isBlank(attr(attribute))
    }

    companion object {
        // todo needs a bit of a chunky cleanup. this level of detail isn't needed
        const val PUBLIC_KEY = "PUBLIC"
        const val SYSTEM_KEY = "SYSTEM"
        private const val NAME = "name"
        private const val PUB_SYS_KEY = "pubSysKey" // PUBLIC or SYSTEM
        private const val PUBLIC_ID = "publicId"
        private const val SYSTEM_ID = "systemId"
    }
}
