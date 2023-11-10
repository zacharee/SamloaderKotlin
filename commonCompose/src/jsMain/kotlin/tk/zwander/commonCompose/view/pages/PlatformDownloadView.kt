package tk.zwander.commonCompose.view.pages

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.data.PlatformFile
import tk.zwander.common.util.fileHandling.GetHandleOptions
import tk.zwander.common.util.fileHandling.showDirectoryPicker
import tk.zwander.common.util.fileHandling.toVfsFile

/**
 * Delegate retrieving the download location to the platform.
 */
actual object PlatformDownloadView {
    actual suspend fun getInput(
        fileName: String,
        callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit
    ) {
        val decName = fileName.replace(".enc2", "").replace(".enc4", "")

        val dirHandle = window.showDirectoryPicker().await()
        val encHandle = dirHandle.getFileHandle(
            fileName,
            object : GetHandleOptions {
                override val create: Boolean
                    get() = true
            }
        ).await()
        val decHandle = dirHandle.getFileHandle(
            decName,
            object : GetHandleOptions {
                override val create: Boolean
                    get() = true
            }
        ).await()

        val encFile = encHandle.toVfsFile()
        val decFile = decHandle.toVfsFile()

       coroutineScope {
           callback(
               DownloadFileInfo(
                   downloadFile = PlatformFile(encFile),
                   decryptFile = PlatformFile(decFile)
               )
           )
       }
    }

    actual fun onStart() {}

    actual fun onFinish() {}

    actual fun onProgress(status: String, current: Long, max: Long) {}
}
