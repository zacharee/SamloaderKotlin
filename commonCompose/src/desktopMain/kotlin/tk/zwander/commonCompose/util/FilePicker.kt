package tk.zwander.commonCompose.util

import tk.zwander.common.data.PlatformFile
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

object FilePicker {
    private var frame: Frame? = null

    fun init(frame: Frame) {
        FilePicker.frame = frame
    }

    fun createFile(name: String): PlatformFile? {
        val chooser = JFileChooser().apply {
            dialogType = JFileChooser.SAVE_DIALOG
            selectedFile = File(name)
        }
        return if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            PlatformFile(chooser.selectedFile)
        } else {
            null
        }
    }

    fun pickFile(): PlatformFile? {
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