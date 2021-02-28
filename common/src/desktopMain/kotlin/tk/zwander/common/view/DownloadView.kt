package tk.zwander.common.view

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.util.toAsync
import tk.zwander.common.util.inputAsync
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

actual object PlatformDownloadView {
    actual suspend fun getInput(fileName: String, callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit) {
        coroutineScope {
            val dialog = FileDialog(Frame())
            dialog.mode = FileDialog.SAVE
            dialog.file = fileName
            dialog.isVisible = true

            if (dialog.file == null) {
                callback(null)
            } else {
                val outputFile = File(dialog.directory, dialog.file)
                val decFile = File(dialog.directory, fileName.replace(".enc2", "").replace(".enc4", ""))

                callback(DownloadFileInfo(
                    outputFile.absolutePath,
                    FileOutputStream(outputFile, true).toAsync(),
                    { FileInputStream(outputFile).inputAsync() },
                    outputFile.length(),
                    FileOutputStream(decFile).toAsync()
                ))
            }
        }
    }

    actual suspend fun deleteFile(path: String) {
//        File(path).delete()
    }
}