package tk.zwander.common.util

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import tk.zwander.common.exceptions.NoBinaryFileError

object CrossPlatformBugsnag {
    private val exceptionsAndCausesToIgnore = arrayOf(
        SocketTimeoutException::class.simpleName,
        HttpRequestTimeoutException::class.simpleName,
        ConnectTimeoutException::class.simpleName,
        CancellationException::class.simpleName,
        "SocketException",
        "SSLHandshakeException",
        NoBinaryFileError::class.simpleName,
        "UnresolvedAddressException",
        "UnknownHostException",
        "ForegroundServiceStartNotAllowedException",
        "CertPathValidatorException",
        "ConnectException",
        ClosedReceiveChannelException::class.simpleName,
    ).filterNotNull()

    private val messagesToIgnore = arrayOf(
        "Software caused connection abort",
        "Failed to parse HTTP response: unexpected EOF",
        "Context.startForegroundService() did not then call Service.startForeground()",
        "unexpected end of stream on",
        "\\n not found: limit=0 content=",
    )

    val appPackages = arrayOf(
        "tk.zwander.common",
        "tk.zwander.commonCompose",
        "tk.zwander.samloaderkotlin",
        "jnafilechooser",
        "my.nanihadesuka.compose",
    )

    private fun Throwable.shouldIgnore(): Boolean {
        if (exceptionsAndCausesToIgnore.contains(this::class.simpleName) || messagesToIgnore.contains(message)) {
            return true
        }

        return this.cause?.shouldIgnore() ?: false
    }

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
