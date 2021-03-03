package tk.zwander.common.data

import com.soywiz.klock.DateTimeTz

data class HistoryInfo(
    val date: DateTimeTz,
    val androidVersion: String,
    val firmwareString: String
)
