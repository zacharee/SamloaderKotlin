package tk.zwander.commonCompose.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tk.zwander.common.util.BifrostSettings
import tk.zwander.common.util.SettingsKey

/**
 * A model class to hold information for the various views.
 */
abstract class BaseModel(
    private val modelKey: String,
) {
    companion object {
        private const val MODEL_KEY = "field_model"
        private const val REGION_KEY = "field_region"
        private const val FIRMWARE_KEY = "field_firmware"
        private const val IMEI_SERIAL_KEY = "field_imei_serial"
    }

    /**
     * Device model.
     */
    val model = SettingsKey.String(MODEL_KEY.fullKey, "", BifrostSettings.settings).asMutableStateFlow()

    /**
     * Device region.
     */
    val region = SettingsKey.String(REGION_KEY.fullKey, "", BifrostSettings.settings).asMutableStateFlow()

    /**
     * Firmware string, if available.
     */
    val fw = SettingsKey.String(FIRMWARE_KEY.fullKey, "", BifrostSettings.settings).asMutableStateFlow()

    /**
     * Newline-separated list of IMEIs/serial numbers.
     */
    val imeiSerial = SettingsKey.String(IMEI_SERIAL_KEY.fullKey, "", BifrostSettings.settings).asMutableStateFlow()

    /**
     * Current status, if available.
     */
    val statusText = MutableStateFlow("")

    /**
     * The current speed of the operation.
     */
    val speed = MutableStateFlow(0L)

    /**
     * The current progress of the operation,
     * based on Pair(current, max).
     */
    val progress = MutableStateFlow(0L to 0L)

    private val _jobs = MutableStateFlow(listOf<Job>())

    val hasRunningJobs: Flow<Boolean>
        get() = _jobs.map { it.any { j -> j.isActive } }

    /**
     * A coroutine scope.
     */
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    protected val String.fullKey: String
        get() = "${modelKey}_$this"

    /**
     * Called when a Job should be ended.
     * @param text the text to show in the status message.
     */
    fun endJob(text: String) {
        _jobs.value.forEach {
            it.cancelChildren()
            it.cancel()
        }
        _jobs.value = listOf()

        progress.value = 0L to 0L
        speed.value = 0L
        statusText.value = text

        onEnd(text)
    }

    fun launchJob(block: suspend CoroutineScope.() -> Unit) {
        _jobs.value += scope.launch(block = block)
    }

    /**
     * Sub-classes can override this to perform
     * extra operations when a Job ends.
     */
    protected open fun onEnd(text: String) {}
}
