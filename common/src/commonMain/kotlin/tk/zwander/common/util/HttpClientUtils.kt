package tk.zwander.common.util

import io.ktor.client.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.cookies.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*

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
