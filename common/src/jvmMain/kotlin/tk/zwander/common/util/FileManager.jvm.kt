package tk.zwander.common.util

import dev.zwander.kotlin.file.IPlatformFile
import dev.zwander.kotlin.file.PlatformFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver

actual object FileManager {
    actual suspend fun pickFile(): IPlatformFile? {
        return FileKit.openFilePicker()?.let { PlatformFile(it.file) }
    }

    actual suspend fun pickDirectory(): IPlatformFile? {
        return FileKit.openDirectoryPicker()?.let { PlatformFile(it.file) }
    }

    actual suspend fun saveFile(name: String): IPlatformFile? {
        val dotIndex = name.lastIndexOf('.')
        val baseName = name.slice(0 until dotIndex)
        val extension = name.slice(dotIndex + 1 until name.length)

        return FileKit.openFileSaver(
            suggestedName = baseName,
            extension = extension,
        )?.let { PlatformFile(it.file) }
    }
}