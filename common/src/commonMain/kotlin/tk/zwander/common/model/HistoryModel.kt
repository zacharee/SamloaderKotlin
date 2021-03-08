package tk.zwander.common.model

import androidx.compose.runtime.*
import tk.zwander.common.data.HistoryInfo

/**
 * The model for the History view.
 */
class HistoryModel : BaseModel() {
    /**
     * The historical firmware versions available.
     */
    var historyItems by mutableStateOf(listOf<HistoryInfo>())
}