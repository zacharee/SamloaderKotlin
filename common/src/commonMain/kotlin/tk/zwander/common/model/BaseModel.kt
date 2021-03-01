package tk.zwander.common.model

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren

open class BaseModel {
    var model by mutableStateOf("")
    var region by mutableStateOf("")
    var fw by mutableStateOf("")
    var statusText by mutableStateOf("")

    var speed by mutableStateOf(0L)
    var progress by mutableStateOf(0L to 0L)

    var job by mutableStateOf<Job?>(null)

    var scope = CoroutineScope(Job())

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

    protected open fun onEnd(text: String) {

    }
}