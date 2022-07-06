package tk.zwander.commonCompose.view.pages

import kotlinx.coroutines.CoroutineScope
import tk.zwander.common.data.DecryptFileInfo

/**
 * Delegate getting the decryption input and output to the platform.
 */
actual object PlatformDecryptView {
    actual suspend fun getInput(callback: suspend CoroutineScope.(DecryptFileInfo?) -> Unit) {
    }

    actual fun onStart() {
    }

    actual fun onFinish() {
    }

    actual fun onProgress(status: String, current: Long, max: Long) {
    }
}
