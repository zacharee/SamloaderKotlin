package tk.zwander.commonCompose.util

import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.pickFile
import tk.zwander.common.data.PlatformFile
import tk.zwander.common.util.BifrostSettings
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

object FilePicker {
    private var frame: Frame? = null

    fun init(frame: Frame) {
        FilePicker.frame = frame
    }

    suspend fun createFile(name: String): PlatformFile? {
        return if (BifrostSettings.Keys.useNativeFileDialog()) {
            val dotIndex = name.lastIndexOf('.')
            val baseName = name.slice(0 until dotIndex)
            val extension = name.slice(dotIndex + 1 until name.length)

            FileKit.saveFile(
                baseName = baseName,
                extension = extension,
            )?.let { PlatformFile(it.file) }
        } else {
            val chooser = JFileChooser().apply {
                dialogType = JFileChooser.SAVE_DIALOG
                selectedFile = File(name)
            }
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                chooser.selectedFile?.let { PlatformFile(it) }
            } else {
                null
            }
        }
    }

    suspend fun pickFile(): PlatformFile? {
        return if (BifrostSettings.Keys.useNativeFileDialog()) {
            FileKit.pickFile()?.let { PlatformFile(it.file) }
        } else {
            val chooser = JFileChooser().apply {
                dialogType = JFileChooser.OPEN_DIALOG
            }

            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                PlatformFile(chooser.selectedFile)
            } else {
                null
            }
        }
    }
}
