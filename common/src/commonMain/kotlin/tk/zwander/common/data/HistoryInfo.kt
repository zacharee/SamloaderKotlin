package tk.zwander.common.data

import kotlinx.datetime.LocalDate

/**
 * Data for a firmware history item.
 */
data class HistoryInfo(
    val date: LocalDate?,
    val androidVersion: String?,
    val firmwareString: String
)
