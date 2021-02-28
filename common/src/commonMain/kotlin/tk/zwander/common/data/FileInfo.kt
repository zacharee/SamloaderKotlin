package tk.zwander.common.data

import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream
import kotlinx.io.core.Input
import kotlinx.io.core.Output

data class DecryptFileInfo(
    val fileName: String,
    val inputPath: String,
    val input: AsyncInputStream,
    val inputSize: Long,
    val output: AsyncOutputStream
)

data class DownloadFileInfo(
    val path: String,
    val output: AsyncOutputStream,
    val input: () -> AsyncInputStream,
    val existingSize: Long,
    val decryptOutput: AsyncOutputStream
)