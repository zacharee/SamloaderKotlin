package tk.zwander.common.model

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren

/**
 * A model class to hold information for the various views.
 */
open class BaseModel {
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
    var job by mutableStateOf<Job?>(null)

    /**
     * A coroutine scope.
     */
    var scope = CoroutineScope(Job())

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
     * Sub-classes can override this to perform
     * extra operations when a Job ends.
     */
    protected open fun onEnd(text: String) {

    }
}