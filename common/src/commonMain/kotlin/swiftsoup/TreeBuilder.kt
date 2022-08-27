package swiftsoup

abstract class TreeBuilder {
    var reader = CharacterReader("")
    var tokenizer: Tokenizer = Tokenizer(reader, null)
    var doc: Document = Document("")
    var stack: ArrayList<Element> = arrayListOf()
    var baseUri: String = ""
    var currentToken: Token? = null
    var errors: ParseErrorList = ParseErrorList(0, 0)
    var settings: ParseSettings = ParseSettings(preserveTagCase = false, preserveAttributeCase = false)

    private val start = Token.StartTag()
    private val end = Token.EndTag()

    abstract fun defaultSettings(): ParseSettings

    open fun initializeParse(input: String, baseUri: String, errors: ParseErrorList, settings: ParseSettings) {
        doc = Document(baseUri)
        this.settings = settings
        reader = CharacterReader(input)
        this.errors = errors
        tokenizer = Tokenizer(reader, errors)
        stack = arrayListOf()
        this.baseUri = baseUri
    }

    open fun parse(input: String, baseUri: String, errors: ParseErrorList, settings: ParseSettings): Document {
        initializeParse(input, baseUri, errors, settings)
        runParser()
        return doc
    }

    fun runParser() {
        while (true) {
            val token = tokenizer.read()
            process(token)
            token.reset()

            if (token is Token.EOF) {
                break
            }
        }
    }

    abstract fun process(token: Token): Boolean

    fun processStartTag(name: String): Boolean {
        if (currentToken === start) { // don't recycle an in-use token
            return process(Token.StartTag().name(name))
        }
        return process(start.reset().name(name))
    }

    fun processStartTag(name: String, attrs: Attributes): Boolean {
        if (currentToken === start) { // don't recycle an in-use token
            return process(Token.StartTag().nameAttr(name, attrs))
        }
        start.reset()
        start.nameAttr(name, attrs)
        return process(start)
    }

    fun processEndTag(name: String): Boolean {
        if (currentToken === end) { // don't recycle an in-use token
            return process(Token.EndTag().name(name))
        }

        return process(end.reset().name(name))
    }

    fun currentElement(): Element? {
        val size: Int = stack.size
        return if (size > 0) stack[size - 1] else null
    }
}
