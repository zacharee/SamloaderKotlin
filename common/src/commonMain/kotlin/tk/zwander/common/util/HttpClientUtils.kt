package tk.zwander.common.util

import com.soywiz.korio.util.OS
import io.ktor.client.*

val client: HttpClient
    get() = HttpClient {
        this.expectSuccess = false
    }

val useProxy = OS.isJsBrowser
