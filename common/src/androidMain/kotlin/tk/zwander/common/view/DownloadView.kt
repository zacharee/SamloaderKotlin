package tk.zwander.common.view

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import tk.zwander.common.data.DownloadFileInfo

actual object PlatformDownloadView {
    var getInputCallback: (suspend CoroutineScope.(fileName: String, callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit) -> Unit)? = null

    actual suspend fun getInput(fileName: String, callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit) {
        coroutineScope {
            getInputCallback?.invoke(this, fileName, callback)
        }
    }

    actual suspend fun deleteFile(path: String) {
    }
}