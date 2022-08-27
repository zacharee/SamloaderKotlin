package swiftsoup

class Document(private val location: String) : Element(Tag.valueOf("#root", ParseSettings.htmlDefault), location) {
    enum class QuirksMode {
        noQuirks,
        quirks,
        limitedQuirks
    }


}
