package tk.zwander.samsungfirmwaredownloader.util

import android.content.Context
import android.content.Intent
import android.net.Uri

//Safely launch a URL.
//If no matching Activity is found, silently fail.
fun Context.launchUrl(url: String, forceBrowser: Boolean = false) {
    try {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (forceBrowser) {
            browserIntent.selector = Intent(Intent.ACTION_VIEW, Uri.parse("https://"))
        }

        startActivity(browserIntent)
    } catch (_: Exception) {
    }
}

//Safely start an email draft.
//If no matching email client is found, silently fail.
fun Context.launchEmail(to: String, subject: String?, content: String?) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.setDataAndType(
            Uri.parse(
                "mailto:${Uri.encode(to)}" +
                        "?subject=${Uri.encode(subject ?: "")}" +
                        "?body=${Uri.encode(content ?: "")}",
            ),
            "text/plain",
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
    } catch (_: Exception) {
    }
}