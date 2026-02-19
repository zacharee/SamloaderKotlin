package tk.zwander.samsungfirmwaredownloader.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

//Safely launch a URL.
//If no matching Activity is found, silently fail.
fun Context.launchUrl(url: String, forceBrowser: Boolean = false) {
    try {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, url.toUri())
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (forceBrowser) {
            browserIntent.selector = Intent(Intent.ACTION_VIEW, "https://".toUri())
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
            ("mailto:${Uri.encode(to)}" +
                    "?subject=${Uri.encode(subject ?: "")}" +
                    "?body=${Uri.encode(content ?: "")}").toUri(),
            "text/plain",
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
    } catch (_: Exception) {
    }
}