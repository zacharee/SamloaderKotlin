package swiftsoup

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

        fun consumeCharacterReference()
    }

    private fun error(errorMsg: String) {
        if (errors != null && errors.canAddError) {
            errors.add(ParseError(reader.pos, errorMsg))
        }
    }
}
