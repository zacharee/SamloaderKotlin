package swiftsoup

import io.ktor.utils.io.charsets.*

class OutputSettings {
    enum class Syntax {
        html, xml
    }

    var escapeMode = Entities.EscapeMode.base
    var encoder = Charsets.UTF_8.newEncoder()
    var prettyPrint = true
    var outline = false
    var indentAmount = 1
    var syntax = Syntax.html

    var charset: Charset
        get() = encoder.charset
        set(value) {
            encoder = charset.newEncoder()
        }
}
