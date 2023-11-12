package tk.zwander.commonCompose.view.pages

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import tk.zwander.common.data.DownloadFileInfo

actual object PlatformDownloadView {
    var getInputCallback: (suspend CoroutineScope.(fileName: String, callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit) -> Unit)? = null
    var downloadStartCallback: (() -> Unit)? = null
    var downloadStopCallback: (() -> Unit)? = null
    var downloadProgressCallback: ((status: String, current: Long, max: Long) -> Unit)? = null

    actual suspend fun getInput(fileName: String, callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit) {
        coroutineScope {
            getInputCallback?.invoke(this, fileName, callback)
        }
    }

    actual fun onStart() {
        downloadStartCallback?.invoke()
    }

    actual fun onFinish() {
        downloadStopCallback?.invoke()
    }

    actual fun onProgress(status: String, current: Long, max: Long) {
        downloadProgressCallback?.invoke(status, current, max)
    }
}