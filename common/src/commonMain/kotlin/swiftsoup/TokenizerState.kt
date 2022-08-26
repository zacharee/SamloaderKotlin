package swiftsoup

enum class TokenizerState {
    Data,
    CharacterReferenceInData,
    Rcdata,
    CharacterReferenceInRcdata,
    Rawtext,
    ScriptData,
    PLAINTEXT,
    TagOpen,
    EndTagOpen,
    TagName,
    RcdataLessthanSign,
    RCDATAEndTagOpen,
    RCDATAEndTagName,
    RawtextLessthanSign,
    RawtextEndTagOpen,
    RawtextEndTagName,
    ScriptDataLessthanSign,
    ScriptDataEndTagOpen,
    ScriptDataEndTagName,
    ScriptDataEscapeStart,
    ScriptDataEscapeStartDash,
    ScriptDataEscaped,
    ScriptDataEscapedDash,
    ScriptDataEscapedDashDash,
    ScriptDataEscapedLessthanSign,
    ScriptDataEscapedEndTagOpen,
    ScriptDataEscapedEndTagName,
    ScriptDataDoubleEscapeStart,
    ScriptDataDoubleEscaped,
    ScriptDataDoubleEscapedDash,
    ScriptDataDoubleEscapedDashDash,
    ScriptDataDoubleEscapedLessthanSign,
    ScriptDataDoubleEscapeEnd,
    BeforeAttributeName,
    AttributeName,
    AfterAttributeName,
    BeforeAttributeValue,
    AttributeValue_doubleQuoted,
    AttributeValue_singleQuoted,
    AttributeValue_unquoted,
    AfterAttributeValue_quoted,
    SelfClosingStartTag,
    BogusComment,
    MarkupDeclarationOpen,
    CommentStart,
    CommentStartDash,
    Comment,
    CommentEndDash,
    CommentEnd,
    CommentEndBang,
    Doctype,
    BeforeDoctypeName,
    DoctypeName,
    AfterDoctypeName,
    AfterDoctypePublicKeyword,
    BeforeDoctypePublicIdentifier,
    DoctypePublicIdentifier_doubleQuoted,
    DoctypePublicIdentifier_singleQuoted,
    AfterDoctypePublicIdentifier,
    BetweenDoctypePublicAndSystemIdentifiers,
    AfterDoctypeSystemKeyword,
    BeforeDoctypeSystemIdentifier,
    DoctypeSystemIdentifier_doubleQuoted,
    DoctypeSystemIdentifier_singleQuoted,
    AfterDoctypeSystemIdentifier,
    BogusDoctype,
    CdataSection;

    companion object {
        const val nullScalr = '\u0000'

        val attributeSingleValueCharsSorted = charArrayOf(
            '\'',
            '&',
            nullScalr
        ).apply { sort() }
        val attributeDoubleValueCharsSorted = charArrayOf(
            '"',
            '&',
            nullScalr
        ).apply { sort() }
        val attributeNameCharsSorted = charArrayOf(
            '\t',
            '\n',
            '\r',
            '\u000c',
            ' ',
            '/',
            '=',
            '>',
            nullScalr,
            '"',
            '\'',
            '<'
        ).apply { sorted() }
        val attributeValueUnquoted = charArrayOf(
            '\t',
            '\n',
            '\r',
            '\u000c',
            ' ',
            '&',
            '>',
            nullScalr,
            '"',
            '\'',
            '<',
            '=',
            '`'
        ).apply { sort() }

        const val replacementChar = Tokenizer.replacementChar
        const val replacementStr = Tokenizer.replacementChar.toString()
        const val eof = CharacterReader.EOF
    }

    fun read(t: Tokenizer, r: CharacterReader) {
        when (this) {
            Data -> {
                when (r.current) {
                    '&' -> t.advanceTransition(CharacterReferenceInData)
                }
            }
        }
    }
}
