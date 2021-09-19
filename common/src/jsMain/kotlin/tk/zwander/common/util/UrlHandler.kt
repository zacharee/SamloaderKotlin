package tk.zwander.common.util

import kotlinx.browser.window

actual object UrlHandler {
    actual fun launchUrl(url: String) {
        window.open(url = url, target = "_blank")
    }

    actual fun sendEmail(address: String, subject: String?, content: String?) {
        window.open("mailto:$address?subject=$subject&body=$content")
    }
}