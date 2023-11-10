package tk.zwander.common.data

import korlibs.time.DateTimeTz

/**
 * Data for a firmware history item.
 */
data class HistoryInfo(
    val date: DateTimeTz?,
    val androidVersion: String?,
    val firmwareString: String
)
