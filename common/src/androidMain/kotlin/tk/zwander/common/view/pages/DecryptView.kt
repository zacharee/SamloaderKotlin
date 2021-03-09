package tk.zwander.common.view.pages

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import tk.zwander.common.data.DecryptFileInfo

actual object PlatformDecryptView {
    var decryptCallback: (suspend CoroutineScope.(suspend CoroutineScope.(DecryptFileInfo?) -> Unit) -> Unit)? = null
    var decryptStartCallback: (() -> Unit)? = null
    var decryptStopCallback: (() -> Unit)? = null
    var decryptProgressCallback: ((status: String, current: Long, max: Long) -> Unit)? = null

    actual suspend fun getInput(callback: suspend CoroutineScope.(DecryptFileInfo?) -> Unit) {
        coroutineScope {
            decryptCallback?.invoke(this, callback)
        }
    }

    actual fun onStart() {
        decryptStartCallback?.invoke()
    }

    actual fun onFinish() {
        decryptStopCallback?.invoke()
    }

    actual fun onProgress(status: String, current: Long, max: Long) {
        decryptProgressCallback?.invoke(status, current, max)
    }
}