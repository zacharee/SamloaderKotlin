package swiftsoup

data class ParseError(
    val pos: Int,
    val errorMsg: String
) {
    override fun toString(): String {
        return "$pos: $errorMsg"
    }
}
