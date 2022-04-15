package tk.zwander.commonCompose.view.pages

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import moe.tlaster.kfilepicker.FilePicker
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.data.PlatformFile
import java.io.File

actual object PlatformDownloadView {
    actual suspend fun getInput(
        fileName: String,
        callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit
    ) {
        coroutineScope {
            val file = FilePicker.createFile(fileName)

            if (file == null) {
                callback(null)
            } else {
                val outputFile = File(file.path)
                val decFile =
                    File(file.path.replace(file.name, ""), fileName.replace(".enc2", "").replace(".enc4", ""))

                callback(
                    DownloadFileInfo(
                        PlatformFile(outputFile),
                        PlatformFile(decFile)
                    )
                )
            }
        }
    }

    actual fun onStart() {}
    actual fun onFinish() {}
    actual fun onProgress(status: String, current: Long, max: Long) {}
}