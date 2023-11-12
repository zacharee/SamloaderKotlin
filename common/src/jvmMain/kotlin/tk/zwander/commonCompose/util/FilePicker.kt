package tk.zwander.commonCompose.util

import com.russhwolf.settings.Settings
import tk.zwander.common.data.PlatformFile
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

object FilePicker {
    private val settings by lazy { Settings() }
    private val useNativeDialog: Boolean
        get() = settings.getBoolean("useNativeFileDialog", false)
    private var frame: Frame? = null

    fun init(frame: Frame) {
        FilePicker.frame = frame
    }

    fun createFile(name: String): PlatformFile? {
        if (useNativeDialog) {
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
                PlatformFile(chooser.selectedFile)
            } else {
                null
            }
        }
    }

    fun pickFile(): PlatformFile? {
        if (useNativeDialog) {
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
