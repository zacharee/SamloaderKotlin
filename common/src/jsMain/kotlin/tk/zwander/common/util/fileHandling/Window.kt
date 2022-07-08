package tk.zwander.common.util.fileHandling

import org.w3c.dom.Window
import kotlin.js.Promise

external interface DirectoryPickerOptions {
    val id: String?
    val mode: String
    val startIn: dynamic
}

inline fun Window.showDirectoryPicker(options: DirectoryPickerOptions? = null): Promise<FileSystemDirectoryHandle> =
    asDynamic().showDirectoryPicker(options)
