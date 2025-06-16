package tk.zwander.common.util

import io.ktor.http.encodeURLQueryComponent
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object UrlHandler {
    actual fun launchUrl(url: String, forceBrowser: Boolean) {
        UIApplication.sharedApplication.openURL(
            NSURL.URLWithString(url)!!,
            mapOf<Any?, Any?>(),
            null,
        )
    }

    actual fun sendEmail(address: String, subject: String?, content: String?) {
        val url = buildString {
            append(address)

            val options = mutableListOf<String>()

            if (!subject.isNullOrBlank()) {
                options.add("subject=${subject.encodeURLQueryComponent()}")
            }

            if (!content.isNullOrBlank()) {
                options.add("body=${content.encodeURLQueryComponent()}")
            }

            if (options.isNotEmpty()) {
                append("?${options.joinToString("&")}")
            }
        }

        UIApplication.sharedApplication.openURL(
            NSURL.URLWithString("mailto:$url")!!,
            mapOf<Any?, Any?>(),
            null,
        )
    }
}