package tk.zwander.common.util

expect object UrlHandler {
    fun launchUrl(url: String)
    fun sendEmail(address: String, subject: String? = null, content: String? = null)
}