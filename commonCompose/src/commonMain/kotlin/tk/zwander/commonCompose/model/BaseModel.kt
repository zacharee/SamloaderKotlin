package tk.zwander.commonCompose.model

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import moe.tlaster.precompose.viewmodel.ViewModel

/**
 * A model class to hold information for the various views.
 */
open class BaseModel : ViewModel() {
    /**
     * Device model.
     */
    var model by mutableStateOf("")

    /**
     * Device region.
     */
    var region by mutableStateOf("")

    /**
     * Firmware string, if available.
     */
    var fw by mutableStateOf("")

    /**
     * Current status, if available.
     */
    var statusText by mutableStateOf("")

    /**
     * The current speed of the operation.
     */
    var speed by mutableStateOf(0L)

    /**
     * The current progress of the operation,
     * based on Pair(current, max).
     */
    var progress by mutableStateOf(0L to 0L)

    /**
     * Any Job currently running.
     */
    var job: Job?
        get() = _job
        set(value) {
            onFinish()
            _job = value
            if (value != null) {
                onStart()
            }
        }

    private var _job by mutableStateOf<Job?>(null)

    /**
     * A coroutine scope.
     */
    var scope = CoroutineScope(Dispatchers.Main)

    /**
     * Called when a Job should be ended.
     * @param text the text to show in the status message.
     */
    open val endJob = { text: String ->
        job?.apply {
            cancelChildren()
            cancel()
        }
        job = null
        progress = 0L to 0L
        speed = 0L
        statusText = text

        onEnd(text)
    }

    /**
     * Called when a new Job is set.
     */
    protected open fun onStart() {}

    /**
     * Called when the Job is cleared.
     */
    protected open fun onFinish() {}

    /**
     * Sub-classes can override this to perform
     * extra operations when a Job ends.
     */
    protected open fun onEnd(text: String) {}
}
