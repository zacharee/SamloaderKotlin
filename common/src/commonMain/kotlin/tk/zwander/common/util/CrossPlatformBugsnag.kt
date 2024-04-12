package tk.zwander.common.util

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import tk.zwander.common.exceptions.DownloadError
import tk.zwander.common.exceptions.NoBinaryFileError

object CrossPlatformBugsnag {
    fun notify(e: Throwable) {
        e.printStackTrace()

        if (e is SocketTimeoutException) {
            return
        }

        if (e is HttpRequestTimeoutException) {
            return
        }

        if (e is ConnectTimeoutException) {
            return
        }

        if (e is kotlin.coroutines.cancellation.CancellationException) {
            return
        }

        if (e is DownloadError) {
            if (e.cause is SocketTimeoutException) {
                return
            }

            if (e.cause is CancellationException) {
                return
            }

            if (e.cause is HttpRequestTimeoutException) {
                return
            }

            if (e.cause is ConnectTimeoutException) {
                return
            }

            if (e.cause is NoBinaryFileError) {
                return
            }
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
