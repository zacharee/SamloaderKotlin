package org.jsoup.parser

import com.soywiz.korio.lang.Charset.Companion.appendCodePointV
import com.soywiz.korio.lang.assert
import org.jsoup.format
import org.jsoup.helper.Validate
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Entities

/**
 * Readers the input stream into tokens.
 */
class Tokeniser constructor(// html input
    private val reader: CharacterReader, // errors found while tokenising
    private val errors: ParseErrorList?
) {
    var state: TokeniserState = TokeniserState.Data // current tokenisation state
        private set


    private var emitPending: Token? = null // the token we are about to emit on next read
    private var isEmitPending: Boolean = false


    private var charsString: String? = null // characters pending an emit. Will fall to charsBuilder if more than one
    private val charsBuilder: StringBuilder =
        StringBuilder(1024) // buffers characters to output as one token, if more than one emit per read
    var dataBuffer: StringBuilder = StringBuilder(1024) // buffers data looking for </script>
    var startPending: Token.StartTag = Token.StartTag()
    var endPending: Token.EndTag = Token.EndTag()
    var tagPending: Token.Tag? = startPending // tag we are building up: start or end pending
    var charPending: Token.Character = Token.Character()
    var doctypePending: Token.Doctype = Token.Doctype() // doctype building up
    var commentPending: Token.Comment = Token.Comment() // comment building up


    private var lastStartTag // the last start tag emitted, to test appropriate end tag
            : String? = null


    private var lastStartCloseSeq // "</" + lastStartTag, so we can quickly check for that in RCData
            : String? = null
    private var markupStartPos: Int = 0
    private var charStartPos: Int = Unset // reader pos at the start of markup / characters. updated on state transition
    fun read(): Token? {
        while (!isEmitPending) {
            state.read(this, reader)
        }

        // if emit is pending, a non-character token was found: return any chars in buffer, and leave token for next read:
        val cb: StringBuilder = charsBuilder
        return if (cb.isNotEmpty()) {
            val str: String = cb.toString()
            cb.removeRange(cb.indices)
            val token: Token = charPending.data(str)
            charsString = null
            token
        } else if (charsString != null) {
            val token: Token = charPending.data(charsString!!)
            charsString = null
            token
        } else {
            isEmitPending = false
            assert(emitPending != null)
            emitPending
        }
    }

    fun emit(token: Token?) {
        Validate.isFalse(isEmitPending)
        emitPending = token
        isEmitPending = true
        token!!.startPos(markupStartPos)
        token.endPos(reader.pos())
        charStartPos = Unset
        if (token.type == Token.TokenType.StartTag) {
            val startTag: Token.StartTag? = token as Token.StartTag?
            lastStartTag = startTag!!.tagName
            lastStartCloseSeq = null // only lazy inits
        } else if (token.type == Token.TokenType.EndTag) {
            val endTag: Token.EndTag? = token as Token.EndTag?
            if (endTag!!.hasAttributes()) error("Attributes incorrectly present on end tag [/%s]", endTag.normalName())
        }
    }

    fun emit(str: String?) {
        // buffer strings up until last string token found, to emit only one token for a run of character refs etc.
        // does not set isEmitPending; read checks that
        if (charsString == null) {
            charsString = str
        } else {
            if (charsBuilder.isEmpty()) { // switching to string builder as more than one emit before read
                charsBuilder.append(charsString)
            }
            charsBuilder.append(str)
        }
        charPending.startPos(charStartPos)
        charPending.endPos(reader.pos())
    }

    // variations to limit need to create temp strings
    fun emit(str: StringBuilder?) {
        if (charsString == null) {
            charsString = str.toString()
        } else {
            if (charsBuilder.isEmpty()) {
                charsBuilder.append(charsString)
            }
            charsBuilder.append(str)
        }
        charPending.startPos(charStartPos)
        charPending.endPos(reader.pos())
    }

    fun emit(c: Char) {
        if (charsString == null) {
            charsString = c.toString()
        } else {
            if (charsBuilder.isEmpty()) {
                charsBuilder.append(charsString)
            }
            charsBuilder.append(c)
        }
        charPending.startPos(charStartPos)
        charPending.endPos(reader.pos())
    }

    fun emit(chars: CharArray?) {
        emit((chars)!!.concatToString())
    }

    fun emit(codepoints: IntArray) {
        emit(codepoints.joinToString("", transform = { it.toChar().toString() }))
    }

    fun transition(newState: TokeniserState) {
        // track markup / data position on state transitions
        when (newState) {
            TokeniserState.TagOpen -> markupStartPos = reader.pos()
            TokeniserState.Data -> if (charStartPos == Unset) // don't reset when we are jumping between e.g data -> char ref -> data
                charStartPos = reader.pos()
            else -> {}
        }
        state = newState
    }

    fun advanceTransition(newState: TokeniserState) {
        transition(newState)
        reader.advance()
    }

    private val codepointHolder: IntArray = IntArray(1) // holder to not have to keep creating arrays
    private val multipointHolder: IntArray = IntArray(2)

    fun consumeCharacterReference( additionalAllowedCharacter: Char?, inAttribute: Boolean): IntArray? {
        if (reader.isEmpty) return null
        if (additionalAllowedCharacter != null && additionalAllowedCharacter == reader.current()) return null
        if (reader.matchesAnySorted(notCharRefCharsSorted)) return null
        val codeRef: IntArray = codepointHolder
        reader.mark()
        if (reader.matchConsume("#")) { // numbered
            val isHexMode: Boolean = reader.matchConsumeIgnoreCase("X")
            val numRef: String = if (isHexMode) reader.consumeHexSequence() else reader.consumeDigitSequence()
            if (numRef.isEmpty()) { // didn't match anything
                characterReferenceError("numeric reference with no numerals")
                reader.rewindToMark()
                return null
            }
            reader.unmark()
            if (!reader.matchConsume(";")) characterReferenceError(
                "missing semicolon on [&#%s]",
                (numRef)
            ) // missing semi
            var charval: Int = -1
            try {
                val base: Int = if (isHexMode) 16 else 10
                charval = numRef.toInt(base)
            } catch (ignored: NumberFormatException) {
            } // skip
            if ((charval == -1) || (charval in 0xD800..0xDFFF) || (charval > 0x10FFFF)) {
                characterReferenceError("character [%s] outside of valid range", charval)
                codeRef[0] = replacementChar.code
            } else {
                // fix illegal unicode characters to match browser behavior
                if (charval >= win1252ExtensionsStart && charval < win1252ExtensionsStart + win1252Extensions.size) {
                    characterReferenceError("character [%s] is not a valid unicode code point", charval)
                    charval = win1252Extensions[charval - win1252ExtensionsStart]
                }

                // todo: implement number replacement table
                // todo: check for extra illegal unicode points as parse errors
                codeRef[0] = charval
            }
            return codeRef
        } else { // named
            // get as many letters as possible, and look for matching entities.
            val nameRef: String = reader.consumeLetterThenDigitSequence()
            val looksLegit: Boolean = reader.matches(';')
            // found if a base named entity without a ;, or an extended entity with the ;.
            val found: Boolean =
                (Entities.isBaseNamedEntity(nameRef) || (Entities.isNamedEntity(nameRef) && looksLegit))
            if (!found) {
                reader.rewindToMark()
                if (looksLegit) // named with semicolon
                    characterReferenceError("invalid named reference [%s]", (nameRef))
                return null
            }
            if (inAttribute && (reader.matchesLetter() || reader.matchesDigit() || reader.matchesAny('=', '-', '_'))) {
                // don't want that to match
                reader.rewindToMark()
                return null
            }
            reader.unmark()
            if (!reader.matchConsume(";")) characterReferenceError(
                "missing semicolon on [&%s]",
                (nameRef)
            ) // missing semi
            return when (Entities.codepointsForName(nameRef, multipointHolder)) {
                1 -> {
                    codeRef[0] = multipointHolder[0]
                    codeRef
                }
                2 -> {
                    multipointHolder
                }
                else -> {
                    Validate.fail("Unexpected characters returned for $nameRef")
                    multipointHolder
                }
            }
        }
    }

    fun createTagPending(start: Boolean): Token.Tag? {
        tagPending = if (start) startPending.reset() else endPending.reset()
        return tagPending
    }

    fun emitTagPending() {
        tagPending!!.finaliseTag()
        emit(tagPending)
    }

    fun createCommentPending() {
        commentPending.reset()
    }

    fun emitCommentPending() {
        emit(commentPending)
    }

    fun createBogusCommentPending() {
        commentPending.reset()
        commentPending.bogus = true
    }

    fun createDoctypePending() {
        doctypePending.reset()
    }

    fun emitDoctypePending() {
        emit(doctypePending)
    }

    fun createTempBuffer() {
        Token.reset(dataBuffer)
    }

    val isAppropriateEndTagToken: Boolean
        get() {
            return lastStartTag != null && tagPending!!.name().equals(lastStartTag, ignoreCase = true)
        }


    fun appropriateEndTagName(): String? {
        return lastStartTag // could be null
    }

    /** Returns the closer sequence `</lastStart`  */
    fun appropriateEndTagSeq(): String {
        if (lastStartCloseSeq == null) // reset on start tag emit
            lastStartCloseSeq = "</$lastStartTag"
        return lastStartCloseSeq!!
    }

    fun error(state: TokeniserState?) {
        if (errors!!.canAddError()) errors.add(
            ParseError(
                reader,
                "Unexpected character '%s' in input state [%s]",
                reader.current(),
                state
            )
        )
    }

    fun eofError(state: TokeniserState?) {
        if (errors!!.canAddError()) errors.add(
            ParseError(
                reader,
                "Unexpectedly reached end of file (EOF) in input state [%s]",
                state
            )
        )
    }

    private fun characterReferenceError(message: String, vararg args: Any) {
        if (errors!!.canAddError()) errors.add(
            ParseError(
                reader,
                String.format("Invalid character reference: $message", *args)
            )
        )
    }

    fun error(errorMsg: String) {
        if (errors!!.canAddError()) errors.add(ParseError(reader, errorMsg))
    }

    fun error(errorMsg: String?, vararg args: Any?) {
        if (errors!!.canAddError()) errors.add(ParseError(reader, errorMsg, *args))
    }

    fun currentNodeInHtmlNS(): Boolean {
        // todo: implement namespaces correctly
        return true
        // Element currentNode = currentNode();
        // return currentNode != null && currentNode.namespace().equals("HTML");
    }

    /**
     * Utility method to consume reader and unescape entities found within.
     * @param inAttribute if the text to be unescaped is in an attribute
     * @return unescaped string from reader
     */
    fun unescapeEntities(inAttribute: Boolean): String {
        val builder: StringBuilder = StringUtil.borrowBuilder()
        while (!reader.isEmpty) {
            builder.append(reader.consumeTo('&'))
            if (reader.matches('&')) {
                reader.consume()
                val c: IntArray? = consumeCharacterReference(null, inAttribute)
                if (c == null || c.isEmpty()) builder.append('&') else {
                    builder.appendCodePointV(c.get(0))
                    if (c.size == 2) builder.appendCodePointV(c[1])
                }
            }
        }
        return StringUtil.releaseBuilder(builder)
    }

    companion object {
        const val replacementChar: Char = '\uFFFD' // replaces null character
        private val notCharRefCharsSorted: CharArray = charArrayOf('\t', '\n', '\r', '\u000c', ' ', '<', '&')

        // Some illegal character escapes are parsed by browsers as windows-1252 instead. See issue #1034
        // https://html.spec.whatwg.org/multipage/parsing.html#numeric-character-reference-end-state
        const val win1252ExtensionsStart: Int = 0x80
        val win1252Extensions: IntArray =
            intArrayOf( // we could build this manually, but Windows-1252 is not a standard java charset so that could break on
                // some platforms - this table is verified with a test
                0x20AC, 0x0081, 0x201A, 0x0192, 0x201E, 0x2026, 0x2020, 0x2021,
                0x02C6, 0x2030, 0x0160, 0x2039, 0x0152, 0x008D, 0x017D, 0x008F,
                0x0090, 0x2018, 0x2019, 0x201C, 0x201D, 0x2022, 0x2013, 0x2014,
                0x02DC, 0x2122, 0x0161, 0x203A, 0x0153, 0x009D, 0x017E, 0x0178
            )

        init {
            notCharRefCharsSorted.sort()
        }

        private const val Unset: Int = -1
    }
}
