package swiftsoup

class BooleanAttribute(key: String) : Attribute(key, "") {
    override val isBooleanAttribute: Boolean
        get() = true
}
