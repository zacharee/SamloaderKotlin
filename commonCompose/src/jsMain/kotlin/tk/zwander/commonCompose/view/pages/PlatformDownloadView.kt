package tk.zwander.commonCompose.view.pages

import kotlinx.coroutines.CoroutineScope
import tk.zwander.common.data.DownloadFileInfo

/**
 * Delegate retrieving the download location to the platform.
 */
actual object PlatformDownloadView {
    actual suspend fun getInput(
        fileName: String,
        callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit
    ) {
    }

    actual fun onStart() {
    }

    actual fun onFinish() {
    }

    actual fun onProgress(status: String, current: Long, max: Long) {
    }

}
