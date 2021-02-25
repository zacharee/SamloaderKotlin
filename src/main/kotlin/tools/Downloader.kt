package tools

import com.github.kittinunf.fuel.core.Response
import java.io.DataInputStream
import java.io.File

object Downloader {
    fun download(response: Response, size: Long, output: File, progressCallback: (current: Long, max: Long) -> Unit) {
        val offset = if (output.exists()) output.length() else 0
        val input = DataInputStream(response.body().toStream())

        val chunkSize = 0x10000
        val totalSize = size - offset

        val read = ByteArray(chunkSize)
        output.outputStream().use { writer ->
            var len: Int
            var count = 0L
            while(true) {
                len = input.read(read)
                count += len
                if (len <= 0) break
                writer.write(read, 0, len)

                progressCallback(count, totalSize)
            }
        }
    }
}