package tk.zwander.common.view

import com.soywiz.korio.stream.AsyncOutputStream
import kotlinx.coroutines.CoroutineScope
import tk.zwander.common.data.DownloadFileInfo

actual object PlatformDownloadView {
    var getInputCallback: ((fileName: String, callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit) -> Unit)? = null
    var getDecryptOutputCallback: ((encPath: String, encName: String, callback: suspend CoroutineScope.(AsyncOutputStream) -> Unit) -> Unit)? = null

    actual suspend fun getInput(fileName: String, callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit) {
        getInputCallback?.invoke(fileName, callback)
    }

    actual suspend fun openDecryptOutput(encPath: String, encName: String, callback: suspend CoroutineScope.(AsyncOutputStream) -> Unit) {
        getDecryptOutputCallback?.invoke(encPath, encName, callback)
    }

    actual suspend fun deleteFile(path: String) {
    }
}