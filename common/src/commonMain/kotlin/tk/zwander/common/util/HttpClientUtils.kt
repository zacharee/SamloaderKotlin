package tk.zwander.common.util

import io.ktor.client.*

val client: HttpClient
    get() = HttpClient {
        this.expectSuccess = false
    }
