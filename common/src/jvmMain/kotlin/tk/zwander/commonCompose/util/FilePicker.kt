package tk.zwander.commonCompose.util

import tk.zwander.common.data.PlatformFile
import tk.zwander.common.util.BifrostSettings
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

object FilePicker {
    private var frame: Frame? = null

    fun init(frame: Frame) {
        FilePicker.frame = frame
    }

    fun createFile(name: String): PlatformFile? {
        if (BifrostSettings.Keys.useNativeFileDialog() == true) {
            val dialog = FileDialog(frame).apply {
                mode = FileDialog.SAVE
                file = name
                isVisible = true
            }

            return dialog.files.firstOrNull()?.let { PlatformFile(it) }
        } else {
            val chooser = JFileChooser().apply {
                dialogType = JFileChooser.SAVE_DIALOG
                selectedFile = File(name)
            }
            return if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                chooser.selectedFile?.let { PlatformFile(it) }
            } else {
                null
            }
        }
    }

    fun pickFile(): PlatformFile? {
        if (BifrostSettings.Keys.useNativeFileDialog() == true) {
            val dialog = FileDialog(frame).apply {
                mode = FileDialog.LOAD
                isVisible = true
            }

            return dialog.files.firstOrNull()?.let { PlatformFile(it) }
        } else {
            val chooser = JFileChooser().apply {
                dialogType = JFileChooser.OPEN_DIALOG
            }

            return if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                PlatformFile(chooser.selectedFile)
            } else {
                null
            }
        }
    }
}
