package tk.zwander.common.data.csc

data class CSCItem(
    val code: String,
    val countries: Array<String>,
    val carriers: Array<String>? = null
) {
    companion object {
        operator fun invoke(
            code: String,
            country: String,
            carrier: String? = null
        ): CSCItem {
            return CSCItem(
                code, arrayOf(country), carrier?.run { arrayOf(this) }
            )
        }

        operator fun invoke(
            code: String,
            country: String,
            carriers: Array<String>
        ): CSCItem {
            return CSCItem(
                code, arrayOf(country), carriers
            )
        }

        operator fun invoke(
            code: String,
            countries: Array<String>,
            carrier: String? = null
        ): CSCItem {
            return CSCItem(
                code, countries, carrier?.run { arrayOf(this) }
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CSCItem) return false

        if (code != other.code) return false
        if (!countries.contentEquals(other.countries)) return false
        if (!carriers.contentEquals(other.carriers)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + countries.contentHashCode()
        result = 31 * result + (carriers?.contentHashCode() ?: 0)
        return result
    }
}
