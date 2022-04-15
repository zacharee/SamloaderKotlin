package tk.zwander.commonCompose.view.pages

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.data.PlatformFile
import java.awt.Dialog
import java.awt.FileDialog
import java.io.File
import javax.swing.FocusManager

actual object PlatformDownloadView {
    actual suspend fun getInput(
        fileName: String,
        callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit
    ) {

        coroutineScope {
            val dialog = FileDialog(Dialog(FocusManager.getCurrentManager().focusedWindow))
            dialog.mode = FileDialog.SAVE
            dialog.file = fileName
            dialog.isVisible = true

            if (dialog.file == null) {
                callback(null)
            } else {
                val outputFile = File(dialog.directory, dialog.file)
                val decFile =
                    File(dialog.directory, fileName.replace(".enc2", "").replace(".enc4", ""))

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