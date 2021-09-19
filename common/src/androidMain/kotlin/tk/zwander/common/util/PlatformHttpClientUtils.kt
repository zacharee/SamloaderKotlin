package tk.zwander.common.util

import io.ktor.client.statement.*
import io.ktor.http.*

actual fun HttpResponse.getCookies(): List<Cookie> {
    return setCookie()
}
