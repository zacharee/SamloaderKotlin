package tk.zwander.commonCompose.view.pages

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import tk.zwander.common.data.DownloadFileInfo
import tk.zwander.common.data.PlatformFile
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

actual object PlatformDownloadView {
    private var _window: Frame? = null

    fun setWindow(window: Frame) {
        this._window = window
    }

    actual suspend fun getInput(
        fileName: String,
        callback: suspend CoroutineScope.(DownloadFileInfo?) -> Unit
    ) {
        coroutineScope {
            val file = createFile(name = fileName)

            if (file == null) {
                callback(null)
            } else {
                val decFile = File(file.getAbsolutePath().replace(file.getName(), ""), fileName.replace(".enc2", "").replace(".enc4", ""))

                callback(
                    DownloadFileInfo(
                        file,
                        PlatformFile(decFile)
                    )
                )
            }
        }
    }

    actual fun onStart() {}
    actual fun onFinish() {}
    actual fun onProgress(status: String, current: Long, max: Long) {}

    private fun createFile(name: String): PlatformFile? {
        if (System.getProperty("os.name").contains("nux")) {
            val chooser = JFileChooser().apply {
                dialogType = JFileChooser.SAVE_DIALOG
                selectedFile = File(name)
            }
            return if (chooser.showSaveDialog(_window) == JFileChooser.APPROVE_OPTION) {
                PlatformFile(chooser.selectedFile)
            } else {
                null
            }
        } else {
            val dialog = FileDialog(_window).apply {
                mode = FileDialog.SAVE
                file = name
            }
            dialog.isVisible = true
            return dialog.files.map {
                PlatformFile(
                    file = it
                )
            }.firstOrNull()
        }
    }
}