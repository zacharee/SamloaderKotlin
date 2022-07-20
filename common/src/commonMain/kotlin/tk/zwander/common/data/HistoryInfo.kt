package tk.zwander.common.data

import com.soywiz.klock.DateTimeTz

/**
 * Data for a firmware history item.
 */
data class HistoryInfo(
    val date: DateTimeTz?,
    val androidVersion: String?,
    val firmwareString: String
)
