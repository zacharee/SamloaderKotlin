package tk.zwander.common.util.fileHandling

import com.soywiz.korio.jsObject
import org.w3c.dom.MimeType
import org.w3c.dom.Window
import kotlin.js.Promise

external interface DirectoryPickerOptions {
    val id: String?
    val mode: String
    val startIn: dynamic
}

external interface FilePickerOptions {
    val multiple: Boolean
    val excludeAcceptAllOption: Boolean
    val types: Array<FileType>?
}

external interface FileType {
    val description: String?
    val accept: FileAcceptType
}

external interface FileAcceptType

fun FileAcceptType(vararg items: Pair<String, Array<String>>): FileAcceptType = jsObject(*items)

external interface FileSaverOptions {
    val excludeAcceptAllOption: Boolean?
        get() = definedExternally
    val suggestedName: String?
        get() = definedExternally
    val types: Array<FileType?>?
        get() = definedExternally
}

inline fun Window.showDirectoryPicker(options: DirectoryPickerOptions? = null): Promise<FileSystemDirectoryHandle> =
    asDynamic().showDirectoryPicker(options)

inline fun Window.showOpenFilePicker(options: FilePickerOptions? = null): Promise<Array<FileSystemFileHandle>> =
    asDynamic().showOpenFilePicker(options)

inline fun Window.showSaveFilePicker(options: FileSaverOptions? = null): Promise<FileSystemFileHandle> =
    asDynamic().showSaveFilePicker(options)
