package tk.zwander.common.view

import com.soywiz.korio.stream.toAsync
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.util.toAsync
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual object PlatformDecryptView {
    actual fun getInput(callback: (DecryptFileInfo?) -> Unit) {
        val dialog = FileDialog(Frame())
        dialog.mode = FileDialog.LOAD
        dialog.isVisible = true

        if (dialog.file != null) {
            val input = File(dialog.directory, dialog.file)

            callback(
                DecryptFileInfo(
                    input.name,
                    input.absolutePath,
                    input.inputStream().toAsync(),
                    input.length(),
                    File(input.parentFile, input.nameWithoutExtension).outputStream().toAsync()
                )
            )
        }
    }
}