package tk.zwander.common.util

import io.ktor.http.*

actual object UrlHandler {
    actual fun launchUrl(url: String) {
    }

    actual fun sendEmail(address: String, subject: String?, content: String?) {
        URLBuilder(
            protocol = URLProtocol("mailto", 80),
            host = address,
            parameters = parametersOf(
                "subject" to listOf(subject ?: ""),
                "body" to listOf(content ?: "")
            )
        )
    }
}
