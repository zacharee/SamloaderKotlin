package tk.zwander.common.model

import androidx.compose.runtime.*
import tk.zwander.common.data.HistoryInfo

class HistoryModel : BaseModel() {
    var historyItems by mutableStateOf(listOf<HistoryInfo>())
}