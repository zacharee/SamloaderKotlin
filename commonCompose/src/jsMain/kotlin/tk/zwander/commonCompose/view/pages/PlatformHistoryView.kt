package tk.zwander.commonCompose.view.pages

import tk.zwander.common.data.HistoryInfo

/**
 * Delegate HTML parsing to the platform until there's an MPP library.
 */
actual object PlatformHistoryView {
    actual suspend fun parseHistory(body: String): List<HistoryInfo> {
        TODO("Not yet implemented")
    }
}
