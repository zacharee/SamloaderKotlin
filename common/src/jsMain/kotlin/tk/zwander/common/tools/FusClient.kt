package tk.zwander.common.tools

import io.ktor.client.fetch.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.internal.*
import korlibs.io.jsObject
import korlibs.io.runtime.node.toByteArray
import korlibs.io.stream.AsyncInputStream
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.Uint8Array
import org.w3c.fetch.RequestInit
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resumeWithException

external class AbortController {
    val signal: AbortSignal

    fun abort(reason: Any? = definedExternally)
}

external class AbortSignal {
    companion object {
        fun abort(reason: Any? = definedExternally): AbortSignal
        fun timeout(time: Long): AbortSignal
    }

    val aborted: Boolean
    val reason: Any?

    fun throwIfAborted()
}

@OptIn(DangerousInternalIoApi::class)
actual suspend fun doDownloadFile(client: FusClient, fileName: String, start: Long): Pair<AsyncInputStream, String?> {
    val authV = client.getAuthV()
    val url = client.getDownloadUrl(fileName)
    val controller = AbortController()

    try {
        val response = window.fetch(
            url,
            RequestInit(
                method = "GET",
                headers = jsObject(
                    "Authorization" to authV,
                    "User-Agent" to "Kies2.0_FUS",
                    "Range" to "bytes=$start-"
                )
            ).apply {
                this.asDynamic()["signal"] = controller.signal
            }
        ).await()

        val md5 = response.headers.get("Content-MD5")
        val stream = (response.body as ReadableStream<Uint8Array>).openAsync()

        return stream to md5
    } catch (e: Throwable) {
        controller.abort()

        throw e
    }
}

suspend fun ReadableStream<Uint8Array>.openAsync(): AsyncInputStream {
    val channel = CoroutineScope(coroutineContext).channelFromStream(stream = this@openAsync)
    val stream = object : AsyncInputStream {
        override suspend fun read(buffer: ByteArray, offset: Int, len: Int): Int {
            return channel.readAvailable(buffer, offset, len)
        }

        override suspend fun close() {
            channel.cancel()
            try {
                cancel().await()
            } catch (_: Throwable) {}
        }
    }

    return try {
        stream
    } catch (e: Throwable) {
        stream.close()
        throw e
    }
}

internal fun CoroutineScope.channelFromStream(
    stream: ReadableStream<Uint8Array>
): ByteReadChannel = writer {
    val reader: ReadableStreamDefaultReader<Uint8Array> = stream.getReader()
    while (true) {
        try {
            val chunk = reader.readChunk() ?: break
            channel.writeFully(chunk.toByteArray())
        } catch (cause: Throwable) {
            reader.cancel(cause).await()
            throw cause
        }
    }
}.channel

suspend fun ReadableStreamDefaultReader<Uint8Array>.readChunk(): Uint8Array? =
    suspendCancellableCoroutine { continuation ->
        read().then {
            val chunk = it.value
            val result = if (it.done) null else chunk
            continuation.resumeWith(Result.success(result))
        }.catch { cause ->
            continuation.resumeWithException(cause)
        }
    }
