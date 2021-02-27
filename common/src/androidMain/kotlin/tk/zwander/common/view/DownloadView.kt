package tk.zwander.common.view

import com.soywiz.korio.stream.AsyncOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.io.core.Output
import tk.zwander.common.data.DownloadFileInfo

actual object PlatformDownloadView {
    actual suspend fun getInput(fileName: String, callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit) {
    }
    actual suspend fun openDecryptOutput(encPath: String, encName: String, callback: suspend CoroutineScope.(AsyncOutputStream) -> Unit) {
    }
    actual suspend fun deleteFile(path: String) {
    }
}