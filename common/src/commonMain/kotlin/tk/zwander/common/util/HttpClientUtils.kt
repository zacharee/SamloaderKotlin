package tk.zwander.common.util

import com.soywiz.kmem.Platform
import com.soywiz.kmem.isJsBrowser
import io.ktor.client.*

val client: HttpClient
    get() = HttpClient {
        this.expectSuccess = false
    }

val useProxy = Platform.isJsBrowser
