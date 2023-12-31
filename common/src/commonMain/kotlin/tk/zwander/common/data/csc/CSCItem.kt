package tk.zwander.common.data.csc

data class CSCItem(
    val code: String,
    val countries: List<String>,
    val carriers: List<String>? = null
): Comparable<CSCItem> {
    companion object {
        operator fun invoke(
            code: String,
            country: String,
            carrier: String? = null
        ): CSCItem {
            return CSCItem(
                code, listOf(country), carrier?.run { listOf(this) },
            )
        }

        operator fun invoke(
            code: String,
            country: String,
            carriers: List<String>
        ): CSCItem {
            return CSCItem(
                code, listOf(country), carriers
            )
        }

        operator fun invoke(
            code: String,
            countries: List<String>,
            carrier: String? = null
        ): CSCItem {
            return CSCItem(
                code, countries, carrier?.run { listOf(this) }
            )
        }
    }

    override fun compareTo(other: CSCItem): Int {
        return this.code.compareTo(other.code)
    }
}
