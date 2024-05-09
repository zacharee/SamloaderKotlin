package tk.zwander.common.util

import com.bugsnag.Bugsnag
import com.bugsnag.CustomReport
import com.bugsnag.Report
import com.bugsnag.Severity
import tk.zwander.common.GradleConfig
import java.lang.reflect.Proxy
import java.util.UUID

data class Breadcrumb(
    val time: Long,
    val message: String,
    val data: Map<String?, Any?>,
    val type: BreadcrumbType,
)

@Suppress("MemberVisibilityCanBePrivate")
actual object BugsnagUtils {
    val bugsnag by lazy { Bugsnag(GradleConfig.bugsnagJvmApiKey) }

    private val breadcrumbs = LinkedHashMap<Long, Breadcrumb>()

    @Suppress("UNCHECKED_CAST")
    fun create() {
        val uuid = BifrostSettings.Keys.bugsnagUuid.getAndSetDefaultIfNonExistent(
            UUID.randomUUID().toString()
        )

        bugsnag.setProjectPackages(*CrossPlatformBugsnag.appPackages)
        bugsnag.setAppVersion(GradleConfig.versionName)
        bugsnag.addCallback {
            it.setUserId(uuid)
            it.addToTab("app", "version_code", GradleConfig.versionCode)
            it.addToTab("app", "jdk_architecture", System.getProperty("sun.arch.data.model"))
        }
        val beforeSendSessionClass = Class.forName("com.bugsnag.BeforeSendSession")
        val sessionPayloadClass = Class.forName("com.bugsnag.SessionPayload")
        val diagnosticsClass = Class.forName("com.bugsnag.Diagnostics")
        bugsnag::class.java.getDeclaredMethod("addBeforeSendSession", beforeSendSessionClass)
            .apply {
                isAccessible = true
            }.invoke(
                bugsnag,
                Proxy.newProxyInstance(
                    beforeSendSessionClass.classLoader,
                    arrayOf(beforeSendSessionClass)
                ) { _, _, args ->
                    val payload = args[0]

                    val diagnostics = sessionPayloadClass.getDeclaredField("diagnostics")
                        .apply { isAccessible = true }
                        .get(payload)

                    val userMap = diagnosticsClass.getDeclaredField("user")
                        .apply { isAccessible = true }
                        .get(diagnostics) as HashMap<String, String?>

                    userMap["id"] = uuid

                    null
                }
            )
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
        val report = CustomReport(bugsnag, e)
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
