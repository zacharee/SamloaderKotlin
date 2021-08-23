package tk.zwander.common.util

fun generateProperUrl(useProxy: Boolean, url: String): String {
    return "${if (useProxy) "http://localhost:8080/" else ""}$url"
}