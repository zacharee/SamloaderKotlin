package tk.zwander.commonCompose.view.pages

import kotlinx.coroutines.CoroutineScope
import tk.zwander.common.data.DownloadFileInfo

actual object PlatformDownloadView {
    actual suspend fun getInput(
        fileName: String,
        callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit
    ) {

    }

    actual fun onStart() {}
    actual fun onFinish() {}
    actual fun onProgress(status: String, current: Long, max: Long) {}
}
