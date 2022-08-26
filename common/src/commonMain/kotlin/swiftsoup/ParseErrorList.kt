package swiftsoup

class ParseErrorList(
    private val initialCapacity: Int,
    val maxSize: Int
) {
    companion object {
        private const val INITIAL_CAPACITY = 16

        fun noTracking(): ParseErrorList {
            return ParseErrorList(0, 0)
        }

        fun tracking(maxSize: Int): ParseErrorList {
            return ParseErrorList(INITIAL_CAPACITY, maxSize)
        }
    }

    private val array = arrayListOf<ParseError>()

    val canAddError: Boolean
        get() = array.size < maxSize

    fun add(e: ParseError) {
        array.add(e)
    }

    fun add(index: Int, element: ParseError) {
        array.add(index, element)
    }
}
