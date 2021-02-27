package tk.zwander.common.view

import com.soywiz.korio.stream.AsyncOutputStream
import com.soywiz.korio.stream.AsyncStream
import com.soywiz.korio.stream.AsyncStreamBase
import com.soywiz.korio.stream.toAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.io.core.Output
import kotlinx.io.streams.asInput
import kotlinx.io.streams.asOutput
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.util.toAsync
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream

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

                callback(DownloadFileInfo(
                    outputFile.absolutePath,
                    FileOutputStream(outputFile, true).toAsync(),
                    { FileInputStream(outputFile).toAsync() },
                    outputFile.length()
                ))
            }
        }
    }
    actual suspend fun openDecryptOutput(encPath: String, encName: String, callback: suspend CoroutineScope.(AsyncOutputStream) -> Unit) {
        coroutineScope {
            val encFile = File(encPath)
            val decFile = File(encFile.parentFile, encName.replace(".enc2", "").replace(".enc4", ""))

            callback(decFile.outputStream().toAsync())
        }
    }
    actual suspend fun deleteFile(path: String) {
//        File(path).delete()
    }
}