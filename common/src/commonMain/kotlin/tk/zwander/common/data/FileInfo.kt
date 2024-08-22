package tk.zwander.common.data

import dev.zwander.kotlin.file.IPlatformFile

/**
 * Holds a reference to an encrypted firmware file along with
 * where the decrypted version should be placed.
 */
data class DecryptFileInfo(
    val encFile: IPlatformFile,
    val decFile: IPlatformFile,
)

/**
 * Holds a reference to where an encrypted firmware file should
 * be downloaded along with where it should be decrypted.
 */
data class DownloadFileInfo(
    val downloadFile: IPlatformFile,
    val decryptFile: IPlatformFile,
    val decryptKeyFile: IPlatformFile?,
)