package swiftsoup

import com.soywiz.korio.lang.Charset.Companion.appendCodePointV
import swiftsoup.Token.Companion.reset

class Tokenizer(
    private val reader: CharacterReader,
    private val errors: ParseErrorList?
) {
    companion object {
        const val replacementChar = '\uFFFD'

        private val notCharRefCharsSorted = charArrayOf(
            '\t',
            '\n',
            '\r',
            '\u000c',
            ' ',
            '<',
            '&'
        ).apply { sort() }
    }

    private var state = TokenizerState.Data
    private var emitPending: Token? = null
    private var isEmitPending = false
    private var charsString: String? = null
    private val charsBuilder = StringBuilder(1024)
    val dataBuffer = StringBuilder(1024)

    var tagPending = Token.Tag()
    val startPending = Token.StartTag()
    val endPending = Token.EndTag()
    val charPending = Token.Char()
    val doctypePending = Token.Doctype()
    val commentPending = Token.Comment()

    private var lastStartTag: String? = null
    private var selfClosingFlagAcknowledged = true

    fun read(): Token {
        if (!selfClosingFlagAcknowledged) {
            error("Self closing flag not acknowledged")
            selfClosingFlagAcknowledged = true
        }

        while (!isEmitPending) {
            state.read(this, reader)
        }

        return if (charsBuilder.isNotEmpty()) {
            val str = charsBuilder.toString()
            charsBuilder.clear()
            charsString = null
            charPending.data(str)
        } else if (charsString != null) {
            val token = charPending.data(charsString!!)
            charsString = null
            token
        } else {
            isEmitPending = false
            emitPending!!
        }
    }

    fun emit(token: Token) {
        emitPending = token
        isEmitPending = true

        when (token) {
            is Token.StartTag -> {
                lastStartTag = token.tagName
                if (token.selfClosing) {
                    selfClosingFlagAcknowledged = false
                }
            }
            is Token.EndTag -> {
                if (token.attributes.size != 0) {
                    error("Attributes incorrectly present on end tag")
                }
            }
            else -> {}
        }
    }

    fun emit(string: String) {
        if (charsString == null) {
            charsString = string
        } else {
            if (charsBuilder.isEmpty()) {
                charsBuilder.append(charsString)
            }
            charsBuilder.append(string)
        }
    }

    fun emit(chars: CharArray) {
        emit(chars.concatToString())
    }

    fun emit(char: Char) {
        emit(char.toString())
    }

    fun transition(state: TokenizerState) {
        this.state = state
    }

    fun advanceTransition(state: TokenizerState) {
        reader.advance()
        this.state = state
    }

    fun acknowledgeSelfClosingFlag() {
        selfClosingFlagAcknowledged = true
    }

    fun consumeCharacterReference(additionalAllowedCharacter: Char?, inAttribute: Boolean): CharArray? {
        if (reader.isEmpty) {
            return null
        }

        if (additionalAllowedCharacter != null && additionalAllowedCharacter == reader.current) {
            return null
        }

        if (reader.matchesAny(notCharRefCharsSorted)) {
            return null
        }

        reader.markPos()

        if (reader.matches("#", consume = true)) {
            val isHexMode = reader.matches("X", consume = true, ignoreCase = true)
            val numRef = if (isHexMode) reader.consumeHexSequence() else reader.consumeDigitSequence()

            if (numRef.isEmpty()) {
                characterReferenceError("numeric reference with no numerals")
                reader.rewindToMark()
                return null
            }

            if (!reader.matches(";", consume = true)) {
                characterReferenceError("missing semicolon")
            }

            var charval = -1

            val base = if (isHexMode) 16 else 10
            numRef.toIntOrNull(base)?.let { charval = it }

            return if (charval == -1 || (charval in 0xD800..0xDFFF) || charval > 0x10FFFF) {
                characterReferenceError("character outside of valid range")
                charArrayOf(replacementChar)
            } else {
                charArrayOf(charval.toChar())
            }
        } else {
            val nameRef = reader.consumeLetterThenDigitSequence()
            val looksLegit = reader.matches(";")
            val found = (Entities.isBaseNamedEntity(nameRef) || (Entities.isNamedEntity(nameRef) && looksLegit))

            if (!found) {
                reader.rewindToMark()
                if (looksLegit) {
                    characterReferenceError("invalid named reference $nameRef")
                }
                return null
            }

            if (inAttribute && (reader.matchesLetter() || reader.matchesDigit() || reader.matchesAny(charArrayOf('=', '-', '_')))) {
                reader.rewindToMark()
                return null
            }

            if (!reader.matches(";", consume = true)) {
                characterReferenceError("missing semicolon") // missing semi
            }

            val points = Entities.codepointsForName(nameRef)

            if (points != null) {
                if (points.size > 2) {
                    throw IllegalArgumentException("Unexpected characters returns for $nameRef num: ${points.size}")
                }
                return points
            }

            throw IllegalArgumentException("Entity name not found $nameRef")
        }
    }

    fun createTagPending(start: Boolean): Token.Tag {
        tagPending = if (start) startPending.reset() else endPending.reset()
        return tagPending
    }

    fun emitTagPending() {
        tagPending.finalizeTag()
        emit(tagPending)
    }

    fun createCommentPending() {
        commentPending.reset()
    }

    fun emitCommentPending() {
        emit(commentPending)
    }

    fun createDoctypePending() {
        doctypePending.reset()
    }

    fun emitDoctypePending() {
        emit(doctypePending)
    }

    fun createTempBuffer() {
        reset(dataBuffer)
    }

    fun isAppropriateEndTagToken(): Boolean {
        if (lastStartTag != null) {
            return tagPending.tagName?.equals(lastStartTag, true) == true
        }

        return false
    }

    fun appropriateEndTagName(): String? {
        return lastStartTag
    }

    fun error(state: TokenizerState) {
        if (errors != null && errors.canAddError) {
            errors.add(ParseError(reader.pos, "Unexpected character '${reader.current}' in input state [$state]"))
        }
    }

    fun eofError(state: TokenizerState) {
        if (errors != null && errors.canAddError) {
            errors.add(ParseError(reader.pos, "Unexpectedly reached end of file (EOF) in input state [${state}]"))
        }
    }

    private fun characterReferenceError(message: String) {
        if (errors?.canAddError == true) {
            errors.add(ParseError(reader.pos, "Invalid character reference: $message"))
        }
    }

    private fun error(errorMsg: String) {
        if (errors != null && errors.canAddError) {
            errors.add(ParseError(reader.pos, errorMsg))
        }
    }

    fun currentNodeInHtmlNS(): Boolean {
        return true
    }

    fun unescapeEntities(inAttribute: Boolean): String {
        val builder = StringBuilder()

        while (!reader.isEmpty) {
            builder.append(reader.consumeTo('&'))

            if (reader.matches('&')) {
                reader.consume()

                val c = consumeCharacterReference(null, inAttribute)

                if (c != null) {
                    if (c.isEmpty()) {
                        builder.append('&')
                    } else {
                        builder.appendCodePointV(c[0].code)
                        if (c.size == 2) {
                            builder.appendCodePointV(c[1].code)
                        }
                    }
                } else {
                    builder.append('&')
                }
            }
        }

        return builder.toString()
    }
}
