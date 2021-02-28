package tk.zwander.common.util

import tk.zwander.samsungfirmwaredownloader.App
import tk.zwander.samsungfirmwaredownloader.util.launchEmail
import tk.zwander.samsungfirmwaredownloader.util.launchUrl

actual object UrlHandler {
    actual fun launchUrl(url: String) {
        val context = App.instance
        context?.launchUrl(url)
    }
    actual fun sendEmail(address: String, subject: String?, content: String?) {
        val context = App.instance
        context?.launchEmail(address, subject, content)
    }
}