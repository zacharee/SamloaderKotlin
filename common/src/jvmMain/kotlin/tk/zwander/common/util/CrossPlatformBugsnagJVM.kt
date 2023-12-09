package tk.zwander.common.util

import com.bugsnag.Bugsnag

data class Breadcrumb(
    val time: Long,
    val message: String,
    val data: Map<String?, Any?>,
    val type: BreadcrumbType,
)

actual object BugsnagUtils {
    const val UUID_KEY = "bugsnag_user_id"

    val bugsnag by lazy { Bugsnag("a5b9774e86bc615c2e49a572b8313489") }

    private val breadcrumbs = LinkedHashMap<Long, Breadcrumb>()

    actual fun notify(e: Throwable) {
        val report = bugsnag.buildReport(e)

        breadcrumbs.forEach { (time, data) ->
            report.addToTab("breadcrumbs", "$time", "${data.message}\n\n" +
                    "Type: ${data.type}\n\n" +
                    data.data.entries.joinToString("\n") { "${it.key}==${it.value}" })
        }
        addBreadcrumb(e.message.toString(), mapOf(), BreadcrumbType.ERROR)

        bugsnag.notify(report)
    }

    actual fun addBreadcrumb(
        message: String,
        data: Map<String?, Any?>,
        type: BreadcrumbType,
    ) {
        val time = System.currentTimeMillis()

        breadcrumbs[time] = Breadcrumb(
            time = time,
            message = message,
            data = data,
            type = type,
        )
    }
}
