package tk.zwander.common.util

import io.ktor.util.collections.ConcurrentSet
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.data.DownloadFileInfo

val eventManager: EventManager
    get() = EventManager.getInstance()

class EventManager private constructor() {
    companion object {
        private var instance: EventManager? = null

        fun getInstance(): EventManager {
            return instance ?: EventManager().apply {
                instance = this
            }
        }
    }

    interface EventListener {
        suspend fun onEvent(event: Event)
    }

    private val listeners = ConcurrentSet<EventListener>()

    fun addListener(listener: EventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: EventListener) {
        listeners.remove(listener)
    }

    suspend fun sendEvent(event: Event) {
        coroutineScope {
            listeners.forEach { listener ->
                launch {
                    with(listener) {
                        onEvent(event)
                    }
                }
            }
        }
    }
}

sealed class Event {
    sealed class Download : Event() {
        data class GetInput(
            val fileName: String,
            val decryptKeyFileName: String?,
            val callback: suspend (DownloadFileInfo?) -> Unit,
        ) : Download()
        data class Progress(
            val status: String,
            val current: Long,
            val max: Long,
        ) : Download()
        data object Start : Download()
        data object Finish : Download()
    }
    sealed class Decrypt : Event() {
        data class GetInput(
            val callback: suspend (DecryptFileInfo?) -> Unit,
        ) : Decrypt()
        data class Progress(
            val status: String,
            val current: Long,
            val max: Long,
        ) : Decrypt()
        data object Start : Decrypt()
        data object Finish : Decrypt()
    }
}