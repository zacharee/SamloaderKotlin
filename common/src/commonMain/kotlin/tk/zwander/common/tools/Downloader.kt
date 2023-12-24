package tk.zwander.common.tools

import korlibs.io.stream.AsyncInputStream
import korlibs.io.stream.AsyncOutputStream
import kotlinx.coroutines.*
import tk.zwander.common.util.streamOperationWithProgress

/**
 * Manage downloading firmware.
 */
object Downloader {
    /**
     * Download an encrypted firmware file given an input and output.
     * @param response a stream of the firmware from Samsung's server.
     * @param size the size of the download.
     * @param output where to save the file.
     * @param outputSize the current size of the output file. Used for resuming downloads.
     * @param progressCallback a callback to keep track of th®e download.
     */
    suspend fun download(
        response: AsyncInputStream,
        size: Long,
        output: AsyncOutputStream,
        outputSize: Long,
        progressCallback: suspend CoroutineScope.(current: Long, max: Long, bps: Long) -> Unit,
    ) {
        try {
            streamOperationWithProgress(
                input = response,
                output = output,
                size = size,
                progressOffset = outputSize,
                progressCallback = progressCallback,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
