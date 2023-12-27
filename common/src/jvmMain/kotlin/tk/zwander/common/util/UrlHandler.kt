package tk.zwander.common.util

import java.awt.Desktop
import java.io.IOException
import java.lang.StringBuilder
import java.net.URI
import java.net.URLEncoder

actual object UrlHandler {
    private val desktop = Desktop.getDesktop()

    actual fun launchUrl(url: String, forceBrowser: Boolean) {
        val uri = URI(url)

        try {
            desktop.browse(uri)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: UnsupportedOperationException) {
            e.printStackTrace()
        }
    }
    actual fun sendEmail(address: String, subject: String?, content: String?) {
        val string = StringBuilder()
        string.append("mailto:")
        string.append(address)

        string.append("?subject=${URLEncoder.encode(subject ?: "", Charsets.UTF_8)}")
        string.append("&body=${URLEncoder.encode(content ?: "", Charsets.UTF_8)}")

        val uri = URI(string.toString())

        try {
            desktop.mail(uri)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: UnsupportedOperationException) {
            e.printStackTrace()
        }
    }
}