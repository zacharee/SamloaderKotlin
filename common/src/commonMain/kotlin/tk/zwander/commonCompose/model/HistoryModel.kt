package tk.zwander.commonCompose.model

import kotlinx.coroutines.flow.MutableStateFlow
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.data.changelog.Changelogs

/**
 * The model for the History view.
 */
class HistoryModel : BaseModel("history_model") {
    /**
     * The historical firmware versions available.
     */
    val historyItems = MutableStateFlow(listOf<HistoryInfo>())

    /**
     * The changelog items for this history lookup.
     */
    val changelogs = MutableStateFlow<Changelogs?>(null)
}
