package tk.zwander.common.util

import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*

@OptIn(InternalAPI::class)
val client: HttpClient
    get() = HttpClient {
        this.expectSuccess = false
    }
