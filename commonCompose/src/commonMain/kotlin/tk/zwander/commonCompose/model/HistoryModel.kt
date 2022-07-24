package tk.zwander.commonCompose.model

import androidx.compose.runtime.*
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.data.changelog.Changelogs

/**
 * The model for the History view.
 */
class HistoryModel : BaseModel() {
    /**
     * The historical firmware versions available.
     */
    var historyItems by mutableStateOf(listOf<HistoryInfo>())

    /**
     * The changelog items for this history lookup.
     */
    var changelogs by mutableStateOf<Changelogs?>(null)
}
