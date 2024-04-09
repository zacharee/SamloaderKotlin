@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")

package tk.zwander.common.util

import io.ktor.client.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.statement.*
import io.ktor.utils.io.ByteReadChannel
import korlibs.memory.Platform

val client: HttpClient
    get() = HttpClient {
        this.expectSuccess = false

        install(HttpTimeout)
    }

val useProxy = Platform.isJsBrowser

class CloseableByteReadChannel(
    private val statement: HttpStatement,
    private val response: HttpResponse,
    wrapped: ByteReadChannel,
) : ByteReadChannel by wrapped {
    suspend fun close() {
        with (statement) {
            response.cleanup()
        }
    }
}
