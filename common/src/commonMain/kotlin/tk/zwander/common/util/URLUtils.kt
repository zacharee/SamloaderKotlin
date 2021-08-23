package tk.zwander.common.util

fun generateProperUrl(useProxy: Boolean, url: String): String {
    return "${if (useProxy) "https://cors-proxy-zwander.herokuapp.com/" else ""}$url"
}