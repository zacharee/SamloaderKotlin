package tk.zwander.common.util

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.document
import kotlinx.browser.window

actual fun HttpResponse.getCookies(): List<Cookie> {
    return document.cookie.split(";").map {
        val split = it.split("=")
        Cookie(split[0].trim(), split.slice(1 until split.lastIndex).joinToString("="))
    }
}
