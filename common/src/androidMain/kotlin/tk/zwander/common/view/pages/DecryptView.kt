package tk.zwander.common.view.pages

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import tk.zwander.common.data.DecryptFileInfo

actual object PlatformDecryptView {
    var decryptCallback: (suspend CoroutineScope.(suspend CoroutineScope.(DecryptFileInfo?) -> Unit) -> Unit)? = null

    actual suspend fun getInput(callback: suspend CoroutineScope.(DecryptFileInfo?) -> Unit) {
        coroutineScope {
            decryptCallback?.invoke(this, callback)
        }
    }
}