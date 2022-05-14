package tk.zwander.commonCompose.view.pages

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.data.PlatformFile
import tk.zwander.commonCompose.util.FilePicker
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

actual object PlatformDownloadView {
    actual suspend fun getInput(
        fileName: String,
        callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit
    ) {
        coroutineScope {
            val file = FilePicker.createFile(name = fileName)

            if (file == null) {
                callback(null)
            } else {
                val decFile = PlatformFile(file.getParent(), fileName.replace(".enc2", "").replace(".enc4", ""))

                callback(
                    DownloadFileInfo(
                        file,
                        decFile
                    )
                )
            }
        }
    }

    actual fun onStart() {}
    actual fun onFinish() {}
    actual fun onProgress(status: String, current: Long, max: Long) {}
}