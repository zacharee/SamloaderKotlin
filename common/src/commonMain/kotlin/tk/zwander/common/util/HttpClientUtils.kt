package tk.zwander.common.util

import io.ktor.client.*
import korlibs.memory.Platform

val client: HttpClient
    get() = HttpClient {
        this.expectSuccess = false
    }

val useProxy = Platform.isJsBrowser
