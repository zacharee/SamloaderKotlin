package tk.zwander.common.util

import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.AsyncOutputStream
import io.ktor.client.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.cookies.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.*

expect fun HttpResponse.getCookies(): List<Cookie>

@OptIn(InternalAPI::class)
val client: HttpClient
    get() = HttpClient() {
        install(HttpCookies) {
            println("Installing cookies")
            storage = AcceptAllCookiesStorage()
        }
        install(Auth)
    }

val ByteReadChannel.async: AsyncInputStream
    get() = object : AsyncInputStream {
        override suspend fun close() {
        }

        override suspend fun read(buffer: ByteArray, offset: Int, len: Int): Int {
            return this@async.readAvailable(buffer, offset, len)
        }
    }
