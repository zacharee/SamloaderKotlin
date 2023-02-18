package tk.zwander.commonCompose.model

import kotlinx.coroutines.flow.MutableStateFlow
import tk.zwander.common.data.changelog.Changelog

/**
 * The model for the Downloader view.
 */
class DownloadModel : BaseModel() {
    /**
     * Whether the user is manually inputting firmware.
     */
    val manual = MutableStateFlow(false)

    /**
     * The Android version of automatically-retrieved
     * firmware.
     */
    val osCode = MutableStateFlow("")

    /**
     * The changelog for the auto-retrieved firmware.
     */
    val changelog = MutableStateFlow<Changelog?>(null)

    /**
     * Whether the changelog is expanded.
     */
    val changelogExpanded = MutableStateFlow(false)
}
