package tk.zwander.commonCompose.view.pages

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.data.PlatformFile
import tk.zwander.common.util.fileHandling.*
import kotlin.coroutines.coroutineContext

/**
 * Delegate getting the decryption input and output to the platform.
 */
actual object PlatformDecryptView {
    actual suspend fun getInput(callback: suspend CoroutineScope.(DecryptFileInfo?) -> Unit) {
        val scope = CoroutineScope(coroutineContext)

        val encHandle = window.showOpenFilePicker().await().firstOrNull()

        if (encHandle == null) {
            callback(scope, null)
            return
        }

        val fileName = encHandle.getFile().await().name
        val decName = fileName.replace(".enc2", "").replace(".enc4", "")

        val decHandle = window.showSaveFilePicker(
            object : FileSaverOptions {
                override val suggestedName: String
                    get() = decName
            }
        ).await()

        val encFile = encHandle.toVfsFile()
        val decFile = decHandle.toVfsFile()

        callback(
            scope,
            DecryptFileInfo(PlatformFile(encFile), PlatformFile(decFile))
        )
    }

    actual fun onStart() {}

    actual fun onFinish() {}

    actual fun onProgress(status: String, current: Long, max: Long) {}
}
