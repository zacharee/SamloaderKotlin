package tk.zwander.common.util

import dev.zwander.kotlin.file.IPlatformFile

expect object FileManager {
    suspend fun pickFile(): IPlatformFile?
    suspend fun pickDirectory(): IPlatformFile?
    suspend fun saveFile(name: String): IPlatformFile?

    suspend fun finishDownload(
        encFile: IPlatformFile,
        onProgress: suspend (Long, Long, Long) -> Unit,
    ): IPlatformFile
}
