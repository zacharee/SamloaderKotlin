package org.jsoup.parser

import org.jsoup.nodes.DocumentType

/**
 * States and transition activations for the Tokeniser.
 */
enum class TokeniserState {
    Data {
        // in data state, gather characters until a character reference or tag is found
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.current()) {
                '&' -> t.advanceTransition(CharacterReferenceInData)
                '<' -> t.advanceTransition(TagOpen)
                nullChar -> {
                    t.error(this) // NOT replacement character (oddly?)
                    t.emit(r.consume())
                }

                eof -> t.emit(Token.EOF())
                else -> {
                    val data: String = r.consumeData()
                    t.emit(data)
                }
            }
        }
    },
    CharacterReferenceInData {
        // from & in data
        override fun read(t: Tokeniser, r: CharacterReader) {
            readCharRef(t, Data)
        }
    },
    Rcdata {
        /// handles data in title, textarea etc
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.current()) {
                '&' -> t.advanceTransition(CharacterReferenceInRcdata)
                '<' -> t.advanceTransition(RcdataLessthanSign)
                nullChar -> {
                    t.error(this)
                    r.advance()
                    t.emit(replacementChar)
                }

                eof -> t.emit(Token.EOF())
                else -> {
                    val data: String = r.consumeData()
                    t.emit(data)
                }
            }
        }
    },
    CharacterReferenceInRcdata {
        override fun read(t: Tokeniser, r: CharacterReader) {
            readCharRef(t, Rcdata)
        }
    },
    Rawtext {
        override fun read(t: Tokeniser, r: CharacterReader) {
            readRawData(t, r, this, RawtextLessthanSign)
        }
    },
    ScriptData {
        override fun read(t: Tokeniser, r: CharacterReader) {
            readRawData(t, r, this, ScriptDataLessthanSign)
        }
    },
    PLAINTEXT {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.current()) {
                nullChar -> {
                    t.error(this)
                    r.advance()
                    t.emit(replacementChar)
                }

                eof -> t.emit(Token.EOF())
                else -> {
                    val data: String = r.consumeTo(nullChar)
                    t.emit(data)
                }
            }
        }
    },
    TagOpen {
        // from < in data
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.current()) {
                '!' -> t.advanceTransition(MarkupDeclarationOpen)
                '/' -> t.advanceTransition(EndTagOpen)
                '?' -> {
                    t.createBogusCommentPending()
                    t.transition(BogusComment)
                }

                else -> if (r.matchesAsciiAlpha()) {
                    t.createTagPending(true)
                    t.transition(TagName)
                } else {
                    t.error(this)
                    t.emit('<') // char that got us here
                    t.transition(Data)
                }
            }
        }
    },
    EndTagOpen {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.isEmpty) {
                t.eofError(this)
                t.emit("</")
                t.transition(Data)
            } else if (r.matchesAsciiAlpha()) {
                t.createTagPending(false)
                t.transition(TagName)
            } else if (r.matches('>')) {
                t.error(this)
                t.advanceTransition(Data)
            } else {
                t.error(this)
                t.createBogusCommentPending()
                t.commentPending.append('/') // push the / back on that got us here
                t.transition(BogusComment)
            }
        }
    },
    TagName {
        // from < or </ in data, will have start or end tag pending
        override fun read(t: Tokeniser, r: CharacterReader) {
            // previous TagOpen state did NOT consume, will have a letter char in current
            val tagName: String = r.consumeTagName()
            t.tagPending!!.appendTagName(tagName)
            when (val c: Char = r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeAttributeName)
                '/' -> t.transition(SelfClosingStartTag)
                '<' -> {
                    r.unconsume()
                    t.error(this)
                    t.emitTagPending()
                    t.transition(Data)
                }

                '>' -> {
                    t.emitTagPending()
                    t.transition(Data)
                }

                nullChar -> t.tagPending!!.appendTagName(replacementStr)
                eof -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> t.tagPending!!.appendTagName(c)
            }
        }
    },
    RcdataLessthanSign {
        // from < in rcdata
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matches('/')) {
                t.createTempBuffer()
                t.advanceTransition(RCDATAEndTagOpen)
            } else if (r.matchesAsciiAlpha() && (t.appropriateEndTagName() != null) && !r.containsIgnoreCase(t.appropriateEndTagSeq())) {
                // diverge from spec: got a start tag, but there's no appropriate end tag (</title>), so rather than
                // consuming to EOF; break out here
                t.tagPending = t.createTagPending(false)!!.name(t.appropriateEndTagName())
                t.emitTagPending()
                t.transition(TagOpen) // straight into TagOpen, as we came from < and looks like we're on a start tag
            } else {
                t.emit("<")
                t.transition(Rcdata)
            }
        }
    },
    RCDATAEndTagOpen {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchesAsciiAlpha()) {
                t.createTagPending(false)
                t.tagPending!!.appendTagName(r.current())
                t.dataBuffer.append(r.current())
                t.advanceTransition(RCDATAEndTagName)
            } else {
                t.emit("</")
                t.transition(Rcdata)
            }
        }
    },
    RCDATAEndTagName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchesAsciiAlpha()) {
                val name: String = r.consumeLetterSequence()
                t.tagPending?.appendTagName(name)
                t.dataBuffer.append(name)
                return
            }
            when (r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> if (t.isAppropriateEndTagToken) t.transition(BeforeAttributeName) else anythingElse(
                    t,
                    r
                )

                '/' -> if (t.isAppropriateEndTagToken) t.transition(SelfClosingStartTag) else anythingElse(t, r)
                '>' -> if (t.isAppropriateEndTagToken) {
                    t.emitTagPending()
                    t.transition(Data)
                } else anythingElse(t, r)

                else -> anythingElse(t, r)
            }
        }

        private fun anythingElse(t: Tokeniser, r: CharacterReader) {
            t.emit("</")
            t.emit(t.dataBuffer)
            r.unconsume()
            t.transition(Rcdata)
        }
    },
    RawtextLessthanSign {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matches('/')) {
                t.createTempBuffer()
                t.advanceTransition(RawtextEndTagOpen)
            } else {
                t.emit('<')
                t.transition(Rawtext)
            }
        }
    },
    RawtextEndTagOpen {
        override fun read(t: Tokeniser, r: CharacterReader) {
            readEndTag(t, r, RawtextEndTagName, Rawtext)
        }
    },
    RawtextEndTagName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            handleDataEndTag(t, r, Rawtext)
        }
    },
    ScriptDataLessthanSign {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '/' -> {
                    t.createTempBuffer()
                    t.transition(ScriptDataEndTagOpen)
                }

                '!' -> {
                    t.emit("<!")
                    t.transition(ScriptDataEscapeStart)
                }

                eof -> {
                    t.emit("<")
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> {
                    t.emit("<")
                    r.unconsume()
                    t.transition(ScriptData)
                }
            }
        }
    },
    ScriptDataEndTagOpen {
        override fun read(t: Tokeniser, r: CharacterReader) {
            readEndTag(t, r, ScriptDataEndTagName, ScriptData)
        }
    },
    ScriptDataEndTagName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            handleDataEndTag(t, r, ScriptData)
        }
    },
    ScriptDataEscapeStart {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matches('-')) {
                t.emit('-')
                t.advanceTransition(ScriptDataEscapeStartDash)
            } else {
                t.transition(ScriptData)
            }
        }
    },
    ScriptDataEscapeStartDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matches('-')) {
                t.emit('-')
                t.advanceTransition(ScriptDataEscapedDashDash)
            } else {
                t.transition(ScriptData)
            }
        }
    },
    ScriptDataEscaped {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.isEmpty) {
                t.eofError(this)
                t.transition(Data)
                return
            }
            when (r.current()) {
                '-' -> {
                    t.emit('-')
                    t.advanceTransition(ScriptDataEscapedDash)
                }

                '<' -> t.advanceTransition(ScriptDataEscapedLessthanSign)
                nullChar -> {
                    t.error(this)
                    r.advance()
                    t.emit(replacementChar)
                }

                else -> {
                    val data: String = r.consumeToAny('-', '<', nullChar)
                    t.emit(data)
                }
            }
        }
    },
    ScriptDataEscapedDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.isEmpty) {
                t.eofError(this)
                t.transition(Data)
                return
            }
            when (val c: Char = r.consume()) {
                '-' -> {
                    t.emit(c)
                    t.transition(ScriptDataEscapedDashDash)
                }

                '<' -> t.transition(ScriptDataEscapedLessthanSign)
                nullChar -> {
                    t.error(this)
                    t.emit(replacementChar)
                    t.transition(ScriptDataEscaped)
                }

                else -> {
                    t.emit(c)
                    t.transition(ScriptDataEscaped)
                }
            }
        }
    },
    ScriptDataEscapedDashDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.isEmpty) {
                t.eofError(this)
                t.transition(Data)
                return
            }
            when (val c: Char = r.consume()) {
                '-' -> t.emit(c)
                '<' -> t.transition(ScriptDataEscapedLessthanSign)
                '>' -> {
                    t.emit(c)
                    t.transition(ScriptData)
                }

                nullChar -> {
                    t.error(this)
                    t.emit(replacementChar)
                    t.transition(ScriptDataEscaped)
                }

                else -> {
                    t.emit(c)
                    t.transition(ScriptDataEscaped)
                }
            }
        }
    },
    ScriptDataEscapedLessthanSign {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchesAsciiAlpha()) {
                t.createTempBuffer()
                t.dataBuffer.append(r.current())
                t.emit("<")
                t.emit(r.current())
                t.advanceTransition(ScriptDataDoubleEscapeStart)
            } else if (r.matches('/')) {
                t.createTempBuffer()
                t.advanceTransition(ScriptDataEscapedEndTagOpen)
            } else {
                t.emit('<')
                t.transition(ScriptDataEscaped)
            }
        }
    },
    ScriptDataEscapedEndTagOpen {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchesAsciiAlpha()) {
                t.createTagPending(false)
                t.tagPending!!.appendTagName(r.current())
                t.dataBuffer.append(r.current())
                t.advanceTransition(ScriptDataEscapedEndTagName)
            } else {
                t.emit("</")
                t.transition(ScriptDataEscaped)
            }
        }
    },
    ScriptDataEscapedEndTagName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            handleDataEndTag(t, r, ScriptDataEscaped)
        }
    },
    ScriptDataDoubleEscapeStart {
        override fun read(t: Tokeniser, r: CharacterReader) {
            handleDataDoubleEscapeTag(t, r, ScriptDataDoubleEscaped, ScriptDataEscaped)
        }
    },
    ScriptDataDoubleEscaped {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.current()) {
                '-' -> {
                    t.emit(c)
                    t.advanceTransition(ScriptDataDoubleEscapedDash)
                }

                '<' -> {
                    t.emit(c)
                    t.advanceTransition(ScriptDataDoubleEscapedLessthanSign)
                }

                nullChar -> {
                    t.error(this)
                    r.advance()
                    t.emit(replacementChar)
                }

                eof -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> {
                    val data: String = r.consumeToAny('-', '<', nullChar)
                    t.emit(data)
                }
            }
        }
    },
    ScriptDataDoubleEscapedDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '-' -> {
                    t.emit(c)
                    t.transition(ScriptDataDoubleEscapedDashDash)
                }

                '<' -> {
                    t.emit(c)
                    t.transition(ScriptDataDoubleEscapedLessthanSign)
                }

                nullChar -> {
                    t.error(this)
                    t.emit(replacementChar)
                    t.transition(ScriptDataDoubleEscaped)
                }

                eof -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> {
                    t.emit(c)
                    t.transition(ScriptDataDoubleEscaped)
                }
            }
        }
    },
    ScriptDataDoubleEscapedDashDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '-' -> t.emit(c)
                '<' -> {
                    t.emit(c)
                    t.transition(ScriptDataDoubleEscapedLessthanSign)
                }

                '>' -> {
                    t.emit(c)
                    t.transition(ScriptData)
                }

                nullChar -> {
                    t.error(this)
                    t.emit(replacementChar)
                    t.transition(ScriptDataDoubleEscaped)
                }

                eof -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> {
                    t.emit(c)
                    t.transition(ScriptDataDoubleEscaped)
                }
            }
        }
    },
    ScriptDataDoubleEscapedLessthanSign {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matches('/')) {
                t.emit('/')
                t.createTempBuffer()
                t.advanceTransition(ScriptDataDoubleEscapeEnd)
            } else {
                t.transition(ScriptDataDoubleEscaped)
            }
        }
    },
    ScriptDataDoubleEscapeEnd {
        override fun read(t: Tokeniser, r: CharacterReader) {
            handleDataDoubleEscapeTag(t, r, ScriptDataEscaped, ScriptDataDoubleEscaped)
        }
    },
    BeforeAttributeName {
        // from tagname <xxx
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '/' -> t.transition(SelfClosingStartTag)
                '<' -> {
                    r.unconsume()
                    t.error(this)
                    t.emitTagPending()
                    t.transition(Data)
                }

                '>' -> {
                    t.emitTagPending()
                    t.transition(Data)
                }

                nullChar -> {
                    r.unconsume()
                    t.error(this)
                    t.tagPending!!.newAttribute()
                    t.transition(AttributeName)
                }

                eof -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                '"', '\'', '=' -> {
                    t.error(this)
                    t.tagPending!!.newAttribute()
                    t.tagPending!!.appendAttributeName(c)
                    t.transition(AttributeName)
                }

                else -> {
                    t.tagPending!!.newAttribute()
                    r.unconsume()
                    t.transition(AttributeName)
                }
            }
        }
    },
    AttributeName {
        // from before attribute name
        override fun read(t: Tokeniser, r: CharacterReader) {
            val name: String =
                r.consumeToAnySorted(*attributeNameCharsSorted) // spec deviate - consume and emit nulls in one hit vs stepping
            t.tagPending?.appendAttributeName(name)
            when (val c: Char = r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(AfterAttributeName)
                '/' -> t.transition(SelfClosingStartTag)
                '=' -> t.transition(BeforeAttributeValue)
                '>' -> {
                    t.emitTagPending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                '"', '\'', '<' -> {
                    t.error(this)
                    t.tagPending!!.appendAttributeName(c)
                }

                else -> t.tagPending!!.appendAttributeName(c)
            }
        }
    },
    AfterAttributeName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '/' -> t.transition(SelfClosingStartTag)
                '=' -> t.transition(BeforeAttributeValue)
                '>' -> {
                    t.emitTagPending()
                    t.transition(Data)
                }

                nullChar -> {
                    t.error(this)
                    t.tagPending!!.appendAttributeName(replacementChar)
                    t.transition(AttributeName)
                }

                eof -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                '"', '\'', '<' -> {
                    t.error(this)
                    t.tagPending!!.newAttribute()
                    t.tagPending!!.appendAttributeName(c)
                    t.transition(AttributeName)
                }

                else -> {
                    t.tagPending!!.newAttribute()
                    r.unconsume()
                    t.transition(AttributeName)
                }
            }
        }
    },
    BeforeAttributeValue {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '"' -> t.transition(AttributeValue_doubleQuoted)
                '&' -> {
                    r.unconsume()
                    t.transition(AttributeValue_unquoted)
                }

                '\'' -> t.transition(AttributeValue_singleQuoted)
                nullChar -> {
                    t.error(this)
                    t.tagPending!!.appendAttributeValue(replacementChar)
                    t.transition(AttributeValue_unquoted)
                }

                eof -> {
                    t.eofError(this)
                    t.emitTagPending()
                    t.transition(Data)
                }

                '>' -> {
                    t.error(this)
                    t.emitTagPending()
                    t.transition(Data)
                }

                '<', '=', '`' -> {
                    t.error(this)
                    t.tagPending!!.appendAttributeValue(c)
                    t.transition(AttributeValue_unquoted)
                }

                else -> {
                    r.unconsume()
                    t.transition(AttributeValue_unquoted)
                }
            }
        }
    },
    AttributeValue_doubleQuoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val value: String = r.consumeAttributeQuoted(false)
            if (value.isNotEmpty()) t.tagPending!!.appendAttributeValue(value) else t.tagPending!!.setEmptyAttributeValue()
            when (val c: Char = r.consume()) {
                '"' -> t.transition(AfterAttributeValue_quoted)
                '&' -> {
                    val ref: IntArray? = t.consumeCharacterReference('"', true)
                    if (ref != null) t.tagPending!!.appendAttributeValue(ref) else t.tagPending!!.appendAttributeValue(
                        '&'
                    )
                }

                nullChar -> {
                    t.error(this)
                    t.tagPending!!.appendAttributeValue(replacementChar)
                }

                eof -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> t.tagPending!!.appendAttributeValue(c)
            }
        }
    },
    AttributeValue_singleQuoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val value: String = r.consumeAttributeQuoted(true)
            if (value.isNotEmpty()) t.tagPending!!.appendAttributeValue(value) else t.tagPending!!.setEmptyAttributeValue()
            when (val c: Char = r.consume()) {
                '\'' -> t.transition(AfterAttributeValue_quoted)
                '&' -> {
                    val ref: IntArray? = t.consumeCharacterReference('\'', true)
                    if (ref != null) t.tagPending!!.appendAttributeValue(ref) else t.tagPending!!.appendAttributeValue(
                        '&'
                    )
                }

                nullChar -> {
                    t.error(this)
                    t.tagPending!!.appendAttributeValue(replacementChar)
                }

                eof -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> t.tagPending!!.appendAttributeValue(c)
            }
        }
    },
    AttributeValue_unquoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val value: String = r.consumeToAnySorted(*attributeValueUnquoted)
            if (value.isNotEmpty()) t.tagPending!!.appendAttributeValue(value)
            when (val c: Char = r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeAttributeName)
                '&' -> {
                    val ref: IntArray? = t.consumeCharacterReference('>', true)
                    if (ref != null) t.tagPending!!.appendAttributeValue(ref) else t.tagPending!!.appendAttributeValue(
                        '&'
                    )
                }

                '>' -> {
                    t.emitTagPending()
                    t.transition(Data)
                }

                nullChar -> {
                    t.error(this)
                    t.tagPending!!.appendAttributeValue(replacementChar)
                }

                eof -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                '"', '\'', '<', '=', '`' -> {
                    t.error(this)
                    t.tagPending!!.appendAttributeValue(c)
                }

                else -> t.tagPending!!.appendAttributeValue(c)
            }
        }
    },  // CharacterReferenceInAttributeValue state handled inline
    AfterAttributeValue_quoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeAttributeName)
                '/' -> t.transition(SelfClosingStartTag)
                '>' -> {
                    t.emitTagPending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> {
                    r.unconsume()
                    t.error(this)
                    t.transition(BeforeAttributeName)
                }
            }
        }
    },
    SelfClosingStartTag {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '>' -> {
                    t.tagPending?.isSelfClosing = true
                    t.emitTagPending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.transition(Data)
                }

                else -> {
                    r.unconsume()
                    t.error(this)
                    t.transition(BeforeAttributeName)
                }
            }
        }
    },
    BogusComment {
        override fun read(t: Tokeniser, r: CharacterReader) {
            // todo: handle bogus comment starting from eof. when does that trigger?
            t.commentPending.append(r.consumeTo('>'))
            // todo: replace nullChar with replaceChar
            val next: Char = r.current()
            if (next == '>' || next == eof) {
                r.consume()
                t.emitCommentPending()
                t.transition(Data)
            }
        }
    },
    MarkupDeclarationOpen {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchConsume("--")) {
                t.createCommentPending()
                t.transition(CommentStart)
            } else if (r.matchConsumeIgnoreCase("DOCTYPE")) {
                t.transition(Doctype)
            } else if (r.matchConsume("[CDATA[")) {
                // todo: should actually check current namespace, and only non-html allows cdata. until namespace
                // is implemented properly, keep handling as cdata
                //} else if (!t.currentNodeInHtmlNS() && r.matchConsume("[CDATA[")) {
                t.createTempBuffer()
                t.transition(CdataSection)
            } else {
                t.error(this)
                t.createBogusCommentPending()
                t.transition(BogusComment)
            }
        }
    },
    CommentStart {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '-' -> t.transition(CommentStartDash)
                nullChar -> {
                    t.error(this)
                    t.commentPending.append(replacementChar)
                    t.transition(Comment)
                }

                '>' -> {
                    t.error(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                else -> {
                    r.unconsume()
                    t.transition(Comment)
                }
            }
        }
    },
    CommentStartDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '-' -> t.transition(CommentEnd)
                nullChar -> {
                    t.error(this)
                    t.commentPending.append(replacementChar)
                    t.transition(Comment)
                }

                '>' -> {
                    t.error(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                else -> {
                    t.commentPending.append(c)
                    t.transition(Comment)
                }
            }
        }
    },
    Comment {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.current()) {
                '-' -> t.advanceTransition(CommentEndDash)
                nullChar -> {
                    t.error(this)
                    r.advance()
                    t.commentPending.append(replacementChar)
                }

                eof -> {
                    t.eofError(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                else -> t.commentPending.append(r.consumeToAny('-', nullChar))
            }
        }
    },
    CommentEndDash {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '-' -> t.transition(CommentEnd)
                nullChar -> {
                    t.error(this)
                    t.commentPending.append('-').append(replacementChar)
                    t.transition(Comment)
                }

                eof -> {
                    t.eofError(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                else -> {
                    t.commentPending.append('-').append(c)
                    t.transition(Comment)
                }
            }
        }
    },
    CommentEnd {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '>' -> {
                    t.emitCommentPending()
                    t.transition(Data)
                }

                nullChar -> {
                    t.error(this)
                    t.commentPending.append("--").append(replacementChar)
                    t.transition(Comment)
                }

                '!' -> t.transition(CommentEndBang)
                '-' -> t.commentPending.append('-')
                eof -> {
                    t.eofError(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                else -> {
                    t.commentPending.append("--").append(c)
                    t.transition(Comment)
                }
            }
        }
    },
    CommentEndBang {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '-' -> {
                    t.commentPending.append("--!")
                    t.transition(CommentEndDash)
                }

                '>' -> {
                    t.emitCommentPending()
                    t.transition(Data)
                }

                nullChar -> {
                    t.error(this)
                    t.commentPending.append("--!").append(replacementChar)
                    t.transition(Comment)
                }

                eof -> {
                    t.eofError(this)
                    t.emitCommentPending()
                    t.transition(Data)
                }

                else -> {
                    t.commentPending.append("--!").append(c)
                    t.transition(Comment)
                }
            }
        }
    },
    Doctype {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeDoctypeName)
                eof -> {
                    t.eofError(this)
                    t.error(this)
                    t.createDoctypePending()
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                '>' -> {
                    t.error(this)
                    t.createDoctypePending()
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.transition(BeforeDoctypeName)
                }
            }
        }
    },
    BeforeDoctypeName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchesAsciiAlpha()) {
                t.createDoctypePending()
                t.transition(DoctypeName)
                return
            }
            when (val c: Char = r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                nullChar -> {
                    t.error(this)
                    t.createDoctypePending()
                    t.doctypePending.name.append(replacementChar)
                    t.transition(DoctypeName)
                }

                eof -> {
                    t.eofError(this)
                    t.createDoctypePending()
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.createDoctypePending()
                    t.doctypePending.name.append(c)
                    t.transition(DoctypeName)
                }
            }
        }
    },
    DoctypeName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.matchesLetter()) {
                val name: String = r.consumeLetterSequence()
                t.doctypePending.name.append(name)
                return
            }
            when (val c: Char = r.consume()) {
                '>' -> {
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(AfterDoctypeName)
                nullChar -> {
                    t.error(this)
                    t.doctypePending.name.append(replacementChar)
                }

                eof -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> t.doctypePending.name.append(c)
            }
        }
    },
    AfterDoctypeName {
        override fun read(t: Tokeniser, r: CharacterReader) {
            if (r.isEmpty) {
                t.eofError(this)
                t.doctypePending.isForceQuirks = true
                t.emitDoctypePending()
                t.transition(Data)
                return
            }
            if (r.matchesAny('\t', '\n', '\r', '\u000c', ' ')) r.advance() // ignore whitespace
            else if (r.matches('>')) {
                t.emitDoctypePending()
                t.advanceTransition(Data)
            } else if (r.matchConsumeIgnoreCase(DocumentType.PUBLIC_KEY)) {
                t.doctypePending.pubSysKey = DocumentType.PUBLIC_KEY
                t.transition(AfterDoctypePublicKeyword)
            } else if (r.matchConsumeIgnoreCase(DocumentType.SYSTEM_KEY)) {
                t.doctypePending.pubSysKey = DocumentType.SYSTEM_KEY
                t.transition(AfterDoctypeSystemKeyword)
            } else {
                t.error(this)
                t.doctypePending.isForceQuirks = true
                t.advanceTransition(BogusDoctype)
            }
        }
    },
    AfterDoctypePublicKeyword {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeDoctypePublicIdentifier)
                '"' -> {
                    t.error(this)
                    // set id to empty string
                    t.transition(DoctypePublicIdentifier_doubleQuoted)
                }

                '\'' -> {
                    t.error(this)
                    // set id to empty string
                    t.transition(DoctypePublicIdentifier_singleQuoted)
                }

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.transition(BogusDoctype)
                }
            }
        }
    },
    BeforeDoctypePublicIdentifier {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '"' ->                     // set id to empty string
                    t.transition(DoctypePublicIdentifier_doubleQuoted)

                '\'' ->                     // set id to empty string
                    t.transition(DoctypePublicIdentifier_singleQuoted)

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.transition(BogusDoctype)
                }
            }
        }
    },
    DoctypePublicIdentifier_doubleQuoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '"' -> t.transition(AfterDoctypePublicIdentifier)
                nullChar -> {
                    t.error(this)
                    t.doctypePending.publicIdentifier.append(replacementChar)
                }

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> t.doctypePending.publicIdentifier.append(c)
            }
        }
    },
    DoctypePublicIdentifier_singleQuoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '\'' -> t.transition(AfterDoctypePublicIdentifier)
                nullChar -> {
                    t.error(this)
                    t.doctypePending.publicIdentifier.append(replacementChar)
                }

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> t.doctypePending.publicIdentifier.append(c)
            }
        }
    },
    AfterDoctypePublicIdentifier {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BetweenDoctypePublicAndSystemIdentifiers)
                '>' -> {
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                '"' -> {
                    t.error(this)
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_doubleQuoted)
                }

                '\'' -> {
                    t.error(this)
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_singleQuoted)
                }

                eof -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.transition(BogusDoctype)
                }
            }
        }
    },
    BetweenDoctypePublicAndSystemIdentifiers {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '>' -> {
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                '"' -> {
                    t.error(this)
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_doubleQuoted)
                }

                '\'' -> {
                    t.error(this)
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_singleQuoted)
                }

                eof -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.transition(BogusDoctype)
                }
            }
        }
    },
    AfterDoctypeSystemKeyword {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeDoctypeSystemIdentifier)
                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                '"' -> {
                    t.error(this)
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_doubleQuoted)
                }

                '\'' -> {
                    t.error(this)
                    // system id empty
                    t.transition(DoctypeSystemIdentifier_singleQuoted)
                }

                eof -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                }
            }
        }
    },
    BeforeDoctypeSystemIdentifier {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '"' ->                     // set system id to empty string
                    t.transition(DoctypeSystemIdentifier_doubleQuoted)

                '\'' ->                     // set id to empty string
                    t.transition(DoctypeSystemIdentifier_singleQuoted)

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.transition(BogusDoctype)
                }
            }
        }
    },
    DoctypeSystemIdentifier_doubleQuoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '"' -> t.transition(AfterDoctypeSystemIdentifier)
                nullChar -> {
                    t.error(this)
                    t.doctypePending.systemIdentifier.append(replacementChar)
                }

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> t.doctypePending.systemIdentifier.append(c)
            }
        }
    },
    DoctypeSystemIdentifier_singleQuoted {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (val c: Char = r.consume()) {
                '\'' -> t.transition(AfterDoctypeSystemIdentifier)
                nullChar -> {
                    t.error(this)
                    t.doctypePending.systemIdentifier.append(replacementChar)
                }

                '>' -> {
                    t.error(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> t.doctypePending.systemIdentifier.append(c)
            }
        }
    },
    AfterDoctypeSystemIdentifier {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ' -> {}
                '>' -> {
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                eof -> {
                    t.eofError(this)
                    t.doctypePending.isForceQuirks = true
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {
                    t.error(this)
                    t.transition(BogusDoctype)
                }
            }
        }
    },
    BogusDoctype {
        override fun read(t: Tokeniser, r: CharacterReader) {
            when (r.consume()) {
                '>' -> {
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                eof -> {
                    t.emitDoctypePending()
                    t.transition(Data)
                }

                else -> {}
            }
        }
    },
    CdataSection {
        override fun read(t: Tokeniser, r: CharacterReader) {
            val data: String = r.consumeTo("]]>")
            t.dataBuffer.append(data)
            if (r.matchConsume("]]>") || r.isEmpty) {
                t.emit(Token.CData(t.dataBuffer.toString()))
                t.transition(Data)
            } // otherwise, buffer underrun, stay in data section
        }
    };

    abstract fun read(t: Tokeniser, r: CharacterReader)

    companion object {
        const val nullChar: Char = '\u0000'

        // char searches. must be sorted, used in inSorted. MUST update TokenisetStateTest if more arrays are added.
        val attributeNameCharsSorted: CharArray =
            charArrayOf('\t', '\n', '\u000c', '\r', ' ', '"', '\'', '/', '<', '=', '>')
        val attributeValueUnquoted: CharArray =
            charArrayOf(nullChar, '\t', '\n', '\u000c', '\r', ' ', '"', '&', '\'', '<', '=', '>', '`')
        private const val replacementChar: Char = Tokeniser.replacementChar
        private const val replacementStr: String = Tokeniser.replacementChar.toString()
        private const val eof: Char = CharacterReader.EOF

        /**
         * Handles RawtextEndTagName, ScriptDataEndTagName, and ScriptDataEscapedEndTagName. Same body impl, just
         * different else exit transitions.
         */
        private fun handleDataEndTag(t: Tokeniser, r: CharacterReader, elseTransition: TokeniserState) {
            if (r.matchesLetter()) {
                val name: String = r.consumeLetterSequence()
                t.tagPending?.appendTagName(name)
                t.dataBuffer.append(name)
                return
            }
            var needsExitTransition = false
            if (t.isAppropriateEndTagToken && !r.isEmpty) {
                when (val c: Char = r.consume()) {
                    '\t', '\n', '\r', '\u000c', ' ' -> t.transition(BeforeAttributeName)
                    '/' -> t.transition(SelfClosingStartTag)
                    '>' -> {
                        t.emitTagPending()
                        t.transition(Data)
                    }

                    else -> {
                        t.dataBuffer.append(c)
                        needsExitTransition = true
                    }
                }
            } else {
                needsExitTransition = true
            }
            if (needsExitTransition) {
                t.emit("</")
                t.emit(t.dataBuffer)
                t.transition(elseTransition)
            }
        }

        private fun readRawData(t: Tokeniser, r: CharacterReader, current: TokeniserState, advance: TokeniserState) {
            when (r.current()) {
                '<' -> t.advanceTransition(advance)
                nullChar -> {
                    t.error(current)
                    r.advance()
                    t.emit(replacementChar)
                }

                eof -> t.emit(Token.EOF())
                else -> {
                    val data: String = r.consumeRawData()
                    t.emit(data)
                }
            }
        }

        private fun readCharRef(t: Tokeniser, advance: TokeniserState) {
            val c: IntArray? = t.consumeCharacterReference(null, false)
            if (c == null) t.emit('&') else t.emit(c)
            t.transition(advance)
        }

        private fun readEndTag(t: Tokeniser, r: CharacterReader, a: TokeniserState, b: TokeniserState) {
            if (r.matchesAsciiAlpha()) {
                t.createTagPending(false)
                t.transition(a)
            } else {
                t.emit("</")
                t.transition(b)
            }
        }

        private fun handleDataDoubleEscapeTag(
            t: Tokeniser,
            r: CharacterReader,
            primary: TokeniserState,
            fallback: TokeniserState
        ) {
            if (r.matchesLetter()) {
                val name: String = r.consumeLetterSequence()
                t.dataBuffer.append(name)
                t.emit(name)
                return
            }
            when (val c: Char = r.consume()) {
                '\t', '\n', '\r', '\u000c', ' ', '/', '>' -> {
                    if ((t.dataBuffer.toString() == "script")) t.transition(primary) else t.transition(fallback)
                    t.emit(c)
                }

                else -> {
                    r.unconsume()
                    t.transition(fallback)
                }
            }
        }
    }
}
