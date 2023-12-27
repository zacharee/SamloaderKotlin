package tk.zwander.common.util

import tk.zwander.samsungfirmwaredownloader.App
import tk.zwander.samsungfirmwaredownloader.util.launchEmail
import tk.zwander.samsungfirmwaredownloader.util.launchUrl

actual object UrlHandler {
    actual fun launchUrl(url: String, forceBrowser: Boolean) {
        val context = App.instance
        context?.launchUrl(url, forceBrowser)
    }
    actual fun sendEmail(address: String, subject: String?, content: String?) {
        val context = App.instance
        context?.launchEmail(address, subject, content)
    }
}