package tk.zwander.commonCompose.model

import androidx.compose.runtime.*
import io.ktor.client.utils.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * A model class to hold information for the various views.
 */
open class BaseModel {
    /**
     * Device model.
     */
    val model = MutableStateFlow("")

    /**
     * Device region.
     */
    val region = MutableStateFlow("")

    /**
     * Firmware string, if available.
     */
    val fw = MutableStateFlow("")

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

    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    val hasRunningJobs: Flow<Boolean>
        get() = jobs.map { it.any { j -> j.isActive } }

    /**
     * A coroutine scope.
     */
    @OptIn(InternalAPI::class)
    private val scope = CoroutineScope(Dispatchers.clientDispatcher(5, "Background${this::class.simpleName}"))

    /**
     * Called when a Job should be ended.
     * @param text the text to show in the status message.
     */
    open val endJob = { text: String ->
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
        _jobs.value = _jobs.value + scope.launch(block = block)
    }

    /**
     * Sub-classes can override this to perform
     * extra operations when a Job ends.
     */
    protected open fun onEnd(text: String) {}
}
