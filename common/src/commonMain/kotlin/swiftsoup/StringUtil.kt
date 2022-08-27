package swiftsoup

import com.soywiz.korio.lang.Charset.Companion.appendCodePointV
import com.soywiz.korio.net.URL
import io.ktor.http.*

object StringUtil {
    private val padding = arrayOf(
        "", " ", "  ", "   ", "    ", "     ", "      ", "       ", "        ", "         ", "          "
    )
    private const val empty = ""
    private const val space = " "

    fun padding(width: Int): String {
        if (width <= 0) {
            return empty
        }

        if (width < padding.size) {
            return padding[width]
        }

        return CharArray(width) { ' ' }.concatToString()
    }

    fun normalizeWhitespace(string: String): String {
        val sb = StringBuilder()
        appendNormalizedWhitespace(sb, string, stripLeading = false)
        return sb.toString()
    }

    fun appendNormalizedWhitespace(accum: StringBuilder, string: String, stripLeading: Boolean) {
        var lasWasWhite = false
        var reachedNonWhite = false

        string.forEach { c ->
            if (c.isWhitespace()) {
                if ((stripLeading && !reachedNonWhite) || lasWasWhite) {
                    return@forEach
                }
                accum.append(" ")
                lasWasWhite = true
            } else {
                accum.appendCodePointV(c.code)
                lasWasWhite = false
                reachedNonWhite = true
            }
        }
    }

    fun resolve(base: Url, relUrl: String): Url {
        var base = base

        if (base.pathSegments.isEmpty() && base.fullPath.last() == '/' && base.protocol.name != "file") {
            base = URLBuilder(base).apply {
                appendPathSegments("/")
            }.build()
        }

        return URLBuilder(base).apply {
            this.encodedPath = relUrl
        }.build()
    }

    fun resolve(baseUrl: String, relUrl: String): String {
        val base = URL(baseUrl)
        if (base.scheme == null) {
            val abs = URL(relUrl)
            return if (abs.scheme != null) abs.fullUrl else empty
        } else {
            val url = resolve(Url(baseUrl), relUrl)
            if (URL(url.toString()).scheme != null) {
                return url.toString()
            }

            if (base.scheme != null) {
                return base.fullUrl
            }
        }

        return empty
    }
}
