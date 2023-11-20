package tk.zwander.common.util

import com.bugsnag.android.BreadcrumbType
import com.bugsnag.android.Bugsnag

actual object BugsnagUtils {
    actual fun notify(e: Throwable) {
        Bugsnag.notify(e)
    }

    actual fun addBreadcrumb(
        message: String,
        data: Map<String?, Any?>,
        type: tk.zwander.common.util.BreadcrumbType,
    ) {
        Bugsnag.leaveBreadcrumb(
            message,
            data,
            BreadcrumbType.valueOf(type.name),
        )
    }
}
