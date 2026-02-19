@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")

package tk.zwander.common.util

import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout

val globalHttpClient: HttpClient
    get() = HttpClient(CIO) {
        this.followRedirects = true
        this.expectSuccess = false

        install(HttpTimeout)
    }
