package swiftsoup

enum class HtmlTreeBuilderState {
    Initial,
    BeforeHtml,
    BeforeHead,
    InHead,
    InHeadNoscript,
    AfterHead,
    InBody,
    Text,
    InTable,
    InTableText,
    InCaption,
    InColumnGroup,
    InTableBody,
    InRow,
    InCell,
    InSelect,
    InSelectInTable,
    AfterBody,
    InFrameset,
    AfterFrameset,
    AfterAfterBody,
    AfterAfterFrameset,
    ForeignContent;

    companion object TagSets {
        val outer = arrayOf("head", "body", "html", "br")
        val outer2 = arrayOf("body", "html", "br")
        val outer3 = arrayOf("body", "html")
        val baseEtc = arrayOf("base", "basefont", "bgsound", "command", "link")
        val baseEtc2 = arrayOf("basefont", "bgsound", "link", "meta", "noframes", "style")
        val baseEtc3 = arrayOf("base", "basefont", "bgsound", "link", "meta", "noframes", "script", "style", "title")
        val headNoscript = arrayOf("head", "noscript")
        val table = arrayOf("table", "tbody", "tfoot", "thead", "tr")
        val tableSections = arrayOf("tbody", "tfoot", "thead")
        val tableMix = arrayOf("body", "caption", "col", "colgroup", "html", "tbody", "td", "tfoot", "th", "thead", "tr")
        val tableMix2 = arrayOf("body", "col", "colgroup", "html", "tbody", "td", "tfoot", "th", "thead", "tr")
        val tableMix3 = arrayOf("caption", "col", "colgroup", "tbody", "tfoot", "thead")
        val tableMix4 = arrayOf("body", "caption", "col", "colgroup", "html", "td", "th", "tr")
        val tableMix5 = arrayOf("caption", "col", "colgroup", "tbody", "tfoot", "thead", "tr")
        val tableMix6 = arrayOf("body", "caption", "col", "colgroup", "html", "td", "th")
        val tableMix7 = arrayOf("body", "caption", "col", "colgroup", "html")
        val tableMix8 = arrayOf("caption", "table", "tbody", "tfoot", "thead", "tr", "td", "th")
        val tableRowsAndCols = arrayOf("caption", "col", "colgroup", "tbody", "td", "tfoot", "th", "thead", "tr")
        val thTd = arrayOf("th", "td")
        val inputKeygenTextarea = arrayOf("input", "keygen", "textarea")
    }

    private val nullString = '\u0000'

    private fun isWhitespace(t: Token): Boolean {
        if (t is Token.Char) {
            val data = t.data
            return data.isNullOrBlank()
        }

        return false
    }

    private fun handleRcData(startTag: Token.StartTag, tb: HtmlTreeBuilder) {

    }

    fun process(t: Token, tb: HtmlTreeBuilder): Boolean {
        when (this) {
            Initial -> {
                if (isWhitespace(t)) {
                    return true
                } else if (t is Token.Comment) {
                    tb.insert(t as Token.Comment)
                }
            }
        }
    }
}
