package tk.zwander.common.util

import com.bugsnag.Bugsnag
import com.bugsnag.Report
import com.bugsnag.Severity
import tk.zwander.common.GradleConfig
import java.util.UUID

data class Breadcrumb(
    val time: Long,
    val message: String,
    val data: Map<String?, Any?>,
    val type: BreadcrumbType,
)

actual object BugsnagUtils {
    val bugsnag by lazy { Bugsnag(GradleConfig.bugsnagJvmApiKey) }

    private val breadcrumbs = LinkedHashMap<Long, Breadcrumb>()

    fun create() {
        val uuid = BifrostSettings.Keys.bugsnagUuid.getAndSetDefaultIfNonExistent(UUID.randomUUID().toString())

        bugsnag.setAppVersion(GradleConfig.versionName)
        bugsnag.addCallback {
            it.setUserId(uuid)
            it.addToTab("app", "version_code", GradleConfig.versionCode)
            it.addToTab("app", "jdk_architecture", System.getProperty("sun.arch.data.model"))
        }
        bugsnag.setSendThreads(true)
        bugsnag.startSession()
    }

    fun destroy() {
        bugsnag.close()
    }

    actual fun notify(e: Throwable) {
        notify(e, Severity.WARNING)
    }

    fun notify(e: Throwable, severity: Severity) {
        val report = bugsnag.buildReport(e)
        report.setSeverity(severity)

        breadcrumbs.forEach { (time, data) ->
            report.addToTab("breadcrumbs", "$time", "${data.message}\n\n" +
                    "Type: ${data.type}\n\n" +
                    data.data.entries.joinToString("\n") { "${it.key}==${it.value}" })
        }
        addBreadcrumb(e.message.toString(), mapOf(), BreadcrumbType.ERROR)

        notify(report)
    }

    fun notify(report: Report) {
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
