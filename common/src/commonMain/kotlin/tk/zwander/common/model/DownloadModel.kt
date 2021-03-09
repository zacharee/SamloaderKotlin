package tk.zwander.common.model

import androidx.compose.runtime.*
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
}