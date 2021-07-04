package tk.zwander.common.model

import androidx.compose.runtime.*
import tk.zwander.common.data.changelog.Changelog
import tk.zwander.common.view.pages.PlatformDownloadView

/**
 * The model for the Downloader view.
 */
class DownloadModel : BaseModel() {
    /**
     * Whether the user is manually inputting firmware.
     */
    var manual by mutableStateOf(false)

    /**
     * The Android version of automatically-retrieved
     * firmware.
     */
    var osCode by mutableStateOf("")

    /**
     * The changelog for the auto-retrieved firmware.
     */
    var changelog by mutableStateOf<Changelog?>(null)

    /**
     * Whether the changelog is expanded.
     */
    var changelogExpanded by mutableStateOf(false)
}