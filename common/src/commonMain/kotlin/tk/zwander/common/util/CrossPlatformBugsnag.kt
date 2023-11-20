package tk.zwander.common.util

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException

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
