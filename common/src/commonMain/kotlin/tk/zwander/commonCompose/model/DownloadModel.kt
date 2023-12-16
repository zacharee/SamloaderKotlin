package tk.zwander.commonCompose.model

import korlibs.io.async.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import tk.zwander.common.data.changelog.Changelog
import tk.zwander.common.util.BifrostSettings

/**
 * The model for the Downloader view.
 */
class DownloadModel : BaseModel("download_model") {
    companion object {
        private const val MANUAL_KEY = "field_manual"
    }

    /**
     * Whether the user is manually inputting firmware.
     */
    val manual = MutableStateFlow(BifrostSettings.settings.getBoolean(MANUAL_KEY.fullKey, false))

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

    override suspend fun createExtra() {
        launch(Dispatchers.Unconfined) {
            manual.collect {
                BifrostSettings.settings.putBoolean(MANUAL_KEY.fullKey, it)
            }
        }
    }
}
