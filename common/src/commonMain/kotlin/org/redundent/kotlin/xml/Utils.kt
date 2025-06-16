package org.redundent.kotlin.xml

internal fun escapeValue(value: Any?, xmlVersion: XmlVersion, useCharacterReference: Boolean = false): String? {
	val asString = value?.toString() ?: return null

	if (useCharacterReference) {
		return referenceCharacter(asString)
	}

	return when (xmlVersion) {
		XmlVersion.V10 -> {
			buildString(asString.length) {
				for (char in asString) {
					when (char) {
						'&' -> append("&amp;")
						'<' -> append("&lt;")
						'>' -> append("&gt;")
						'"' -> append("&quot;")
						'\'' -> append("&apos;")
						else -> {
							if (xml10ValidCharCodes.any { range -> char.code in range }) {
								append(char)
							}
						}
					}
				}
			}
		}

		XmlVersion.V11 -> {
			buildString(asString.length) {
				for (char in asString) {
					when (char) {
						'&' -> append("&amp;")
						'<' -> append("&lt;")
						'>' -> append("&gt;")
						'"' -> append("&quot;")
						'\'' -> append("&apos;")
						'\u000b' -> append("&#11;")
						'\u000c' -> append("&#12;")
						else -> {
							if (xml11ValidCharCodes.any { range -> char.code in range }) {
								append(char)
							}
						}
					}
				}
			}
		}
	}
}

private val xml10ValidCharCodes: Set<IntRange> = buildSet {
	add(0x0009..0x0009)
	add(0x000A..0x000A)
	add(0x0020..0xD7FF)
	add(0xE000..0xFFFD)
	add(0x10000..0x10FFFF)
}

private val xml11ValidCharCodes: Set<IntRange> = buildSet {
	add(0x0001..0xD7FF)
	add(0xE000..0xFFFD)
	add(0x10000..0x10FFFF)
}

internal fun referenceCharacter(asString: String): String {
	val builder = StringBuilder()

	asString.toCharArray().forEach { character ->
		when (character) {
			'\'' -> builder.append("&#39;")
			'&' -> builder.append("&#38;")
			'<' -> builder.append("&#60;")
			'>' -> builder.append("&#62;")
			'"' -> builder.append("&#34;")
			else -> builder.append(character)
		}
	}

	return builder.toString()
}

internal fun buildName(name: String, namespace: Namespace?): String =
	if (namespace == null || namespace.isDefault) name else "${namespace.name}:$name"

fun unsafe(value: Any?): Unsafe = Unsafe(value)

internal fun getLineEnding(printOptions: PrintOptions): String =
	if (printOptions.pretty) {
		"\n"
		//		System.lineSeparator()
	} else {
		""
	}
