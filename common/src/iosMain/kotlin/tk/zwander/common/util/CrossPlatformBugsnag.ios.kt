@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")

package tk.zwander.common.util

import com.rickclephas.kmp.nsexceptionkt.core.asNSException
import dev.zwander.bugsnag.cinterop.BSGBreadcrumbType
import dev.zwander.bugsnag.cinterop.Bugsnag
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual object BugsnagUtils {
    actual fun notify(e: Throwable) {
        Bugsnag.notify(e.asNSException(true)) { true }
    }

    actual fun addBreadcrumb(
        message: String,
        data: Map<String?, Any?>,
        type: BreadcrumbType,
    ) {
        Bugsnag.leaveBreadcrumbWithMessage(
            message,
            data.mapKeys { it.key },
            type.toSystem(),
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun BreadcrumbType.toSystem(): BSGBreadcrumbType {
    return when (this) {
        BreadcrumbType.ERROR -> BSGBreadcrumbType.BSGBreadcrumbTypeError
        BreadcrumbType.LOG -> BSGBreadcrumbType.BSGBreadcrumbTypeLog
        BreadcrumbType.MANUAL -> BSGBreadcrumbType.BSGBreadcrumbTypeManual
        BreadcrumbType.NAVIGATION -> BSGBreadcrumbType.BSGBreadcrumbTypeNavigation
        BreadcrumbType.PROCESS -> BSGBreadcrumbType.BSGBreadcrumbTypeProcess
        BreadcrumbType.REQUEST -> BSGBreadcrumbType.BSGBreadcrumbTypeRequest
        BreadcrumbType.STATE -> BSGBreadcrumbType.BSGBreadcrumbTypeState
        BreadcrumbType.USER -> BSGBreadcrumbType.BSGBreadcrumbTypeUser
    }
}