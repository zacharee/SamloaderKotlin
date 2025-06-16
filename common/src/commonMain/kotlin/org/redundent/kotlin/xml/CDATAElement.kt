package org.redundent.kotlin.xml

/**
 * Similar to a [TextElement] except that the inner text is wrapped inside a `<![CDATA[]]>` tag.
 */
class CDATAElement internal constructor(text: String) : TextElement(text) {
	override fun renderedText(printOptions: PrintOptions): String {
		fun String.escapeCData(): String {
			val cdataEnd = "]]>"
			val cdataStart = "<![CDATA["
			return this
				// split cdataEnd into two pieces so XML parser doesn't recognize it
				.replace(cdataEnd, "]]$cdataEnd$cdataStart>")
		}

		return "<![CDATA[${text.escapeCData()}]]>"
	}

	override fun equals(other: Any?): Boolean = super.equals(other) && other is CDATAElement

	// Need to use javaClass here to avoid a normal TextElement and a CDATAElement having the same hashCode if they have
	// the same text
	override fun hashCode(): Int = 31 * super.hashCode() + 31 * this::class.hashCode()
}
