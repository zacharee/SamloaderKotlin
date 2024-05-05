package tk.zwander.common.util

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import korlibs.io.lang.portableSimpleName
import kotlinx.coroutines.CancellationException
import tk.zwander.common.exceptions.NoBinaryFileError

val exceptionsAndCausesToIgnore = arrayOf(
    SocketTimeoutException::class.portableSimpleName,
    HttpRequestTimeoutException::class.portableSimpleName,
    ConnectTimeoutException::class.portableSimpleName,
    CancellationException::class.portableSimpleName,
    "SocketException",
    "SSLHandshakeException",
    NoBinaryFileError::class.portableSimpleName,
    "UnresolvedAddressException",
    "UnknownHostException",
)

private fun Throwable.shouldIgnore(): Boolean {
    if (exceptionsAndCausesToIgnore.contains(this::class.portableSimpleName)) {
        return true
    }

    return this.cause?.shouldIgnore() ?: false
}

object CrossPlatformBugsnag {
    fun notify(e: Throwable) {
        e.printStackTrace()

        if (e.shouldIgnore()) {
            return
        }

        BugsnagUtils.notify(e)
    }
}

expect object BugsnagUtils {
    fun notify(e: Throwable)

    fun addBreadcrumb(
        message: String,
        data: Map<String?, Any?>,
        type: BreadcrumbType,
    )
}

@Suppress("unused")
enum class BreadcrumbType {
    ERROR,
    LOG,
    MANUAL,
    NAVIGATION,
    PROCESS,
    REQUEST,
    STATE,
    USER,
}
