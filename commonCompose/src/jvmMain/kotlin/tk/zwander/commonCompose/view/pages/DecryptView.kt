package tk.zwander.commonCompose.view.pages

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import tk.zwander.common.data.DecryptFileInfo
import tk.zwander.common.data.PlatformFile
import tk.zwander.commonCompose.util.FilePicker
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual object PlatformDecryptView {
    actual suspend fun getInput(callback: suspend CoroutineScope.(DecryptFileInfo?) -> Unit) {
        coroutineScope {
            val file = FilePicker.pickFile()

            if (file != null) {
                callback(
                    DecryptFileInfo(
                        file,
                        PlatformFile(file.getParent(), File(file.getAbsolutePath()).nameWithoutExtension)
                    )
                )
            } else {
                callback(null)
            }
        }
    }

    actual fun onStart() {}
    actual fun onFinish() {}
    actual fun onProgress(status: String, current: Long, max: Long) {}
}