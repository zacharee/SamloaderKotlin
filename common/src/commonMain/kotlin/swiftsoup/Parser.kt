package swiftsoup

class Parser(var treeBuilder: TreeBuilder) {
    companion object {
        private const val DEFAULT_MAX_ERRORS = 0

        fun parse(html: String, baseUri: String): Document {
            val treeBuilder = HtmlTreeBuilder()
            return treeBuilder.parse(html, baseUri, ParseErrorList.noTracking(), treeBuilder.defaultSettings())
        }

        fun parseFragment(fragmentHtml: String, context: Element?, baseUri: String): List<Node> {
            val treeBuilder = HtmlTreeBuilder()
            return treeBuilder.parseFragment(fragmentHtml, context, baseUri, ParseErrorList.noTracking(), treeBuilder.defaultSettings())
        }

        fun parseXmlFragment(fragmentXml: String, baseUri: String): List<Node> {
            val treeBuilder = XmlTreeBuilder()
            return treeBuilder.parseFragment(fragmentXml, baseUri, ParseErrorList.noTracking(), treeBuilder.defaultSettings())
        }

        fun parseBodyFragment(bodyHtml: String, baseUri: String): Document {
            val doc = Document.createShell(baseUri)

            val body = doc.body

            if (body != null) {
                val nodeList = parseFragment(bodyHtml, body, baseUri)

                if (nodeList.isNotEmpty()) {
                    for (i in 1 until nodeList.size) {
                        nodeList[i].remove()
                    }
                }

                nodeList.forEach {
                    body.appendChild(it)
                }
            }

            return doc
        }

        fun unescapeEntities(string: String, inAttribute: Boolean): String {
            return Tokenizer(CharacterReader(string), ParseErrorList.noTracking()).unescapeEntities(inAttribute)
        }

        fun parseBodyFragmentRelaxed(bodyHtml: String, baseUri: String): Document {
            return parse(bodyHtml, baseUri)
        }

        fun htmlParser() = Parser(HtmlTreeBuilder())
        fun xmlParser() = Parser(XmlTreeBuilder())
    }

    var settings = treeBuilder.defaultSettings()
    var maxErrors = DEFAULT_MAX_ERRORS
    var errors = ParseErrorList(16, 16)
        private set

    fun parseInput(html: String, baseUri: String): Document {
        errors = if (isTrackErrors()) ParseErrorList.tracking(maxErrors) else ParseErrorList.noTracking()
        return treeBuilder.parse(html, baseUri, errors, settings)
    }

    fun isTrackErrors() = maxErrors > 0

}
