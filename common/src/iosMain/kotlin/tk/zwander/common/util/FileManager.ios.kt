package tk.zwander.common.util

import dev.zwander.kotlin.file.IPlatformFile
import dev.zwander.kotlin.file.PlatformFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver

actual object FileManager {
    actual suspend fun pickFile(): IPlatformFile? {
        return FileKit.openFilePicker()?.let { PlatformFile(it.nsUrl) }
    }

    actual suspend fun pickDirectory(): IPlatformFile? {
        return FileKit.openDirectoryPicker()?.let { PlatformFile(it.nsUrl) }
    }

    actual suspend fun saveFile(name: String): IPlatformFile? {
        return FileKit.openFileSaver(suggestedName = name)?.let { PlatformFile(it.nsUrl) }
    }
}