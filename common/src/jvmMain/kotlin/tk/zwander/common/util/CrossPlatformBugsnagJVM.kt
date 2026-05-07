package tk.zwander.common.util

import com.bugsnag.Bugsnag
import com.bugsnag.BugsnagEvent
import com.bugsnag.CustomEvent
import com.bugsnag.Severity
import com.bugsnag.ThreadSendPolicy
import oshi.SystemInfo
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
    val oshiSystemInfo by lazy { SystemInfo() }

    private val breadcrumbs = LinkedHashMap<Long, Breadcrumb>()

    @Suppress("UNCHECKED_CAST")
    fun create() {
        val uuid = BifrostSettings.Keys.bugsnagUuid.getAndSetDefaultIfNonExistent(
            UUID.randomUUID().toString()
        )

        bugsnag.setProjectPackages(*CrossPlatformBugsnag.appPackages)
        bugsnag.setAppVersion(GradleConfig.versionName)
        bugsnag.addOnError {
            it.setUserId(uuid)
            it.addMetadata("device", "manufacturer", oshiSystemInfo.hardware.computerSystem.manufacturer)
            it.addMetadata("device", "model", oshiSystemInfo.hardware.computerSystem.model)
            it.addMetadata("device", "memory", oshiSystemInfo.hardware.memory.total)
            it.addMetadata("device", "motherboard", oshiSystemInfo.hardware.computerSystem.baseboard.model)
            it.addMetadata("device", "firmwareVersion", oshiSystemInfo.hardware.computerSystem.firmware.version)
            it.addMetadata("device", "processorModel", oshiSystemInfo.hardware.processor.processorIdentifier.model)
            it.addMetadata("device", "processorFamily", oshiSystemInfo.hardware.processor.processorIdentifier.family)
            it.addMetadata("device", "processorName", oshiSystemInfo.hardware.processor.processorIdentifier.name)
            it.addMetadata("app", "version_code", GradleConfig.versionCode)
            it.addMetadata("app", "jdk_architecture", System.getProperty("sun.arch.data.model"))
            true
        }
        val beforeSendSessionClass = Class.forName("com.bugsnag.OnSession")
        val sessionPayloadClass = Class.forName("com.bugsnag.SessionPayload")
        val diagnosticsClass = Class.forName("com.bugsnag.Diagnostics")
        bugsnag::class.java.getDeclaredMethod("addOnSession", beforeSendSessionClass)
            .apply {
                isAccessible = true
            }.invoke(
                bugsnag,
                Proxy.newProxyInstance(
                    beforeSendSessionClass.classLoader,
                    arrayOf(beforeSendSessionClass),
                ) { _, _, args ->
                    val payload = args[0]

                    val diagnostics = sessionPayloadClass.getDeclaredField("diagnostics")
                        .apply { isAccessible = true }
                        .get(payload)

                    val userMap = diagnosticsClass.getDeclaredField("user")
                        .apply { isAccessible = true }
                        .get(diagnostics) as HashMap<String, String?>

                    userMap["id"] = uuid

                    true
                }
            )
        bugsnag.setSendThreads(ThreadSendPolicy.ALWAYS)
        bugsnag.startSession()
    }

    fun destroy() {
        bugsnag.close()
    }

    actual fun notify(e: Throwable) {
        notify(e, Severity.WARNING)
    }

    fun notify(e: Throwable, severity: Severity) {
        val report = CustomEvent(bugsnag, e)
        report.setSeverity(severity)

        breadcrumbs.forEach { (time, data) ->
            report.addMetadata("breadcrumbs", "$time", "${data.message}\n\n" +
                    "Type: ${data.type}\n\n" +
                    data.data.entries.joinToString("\n") { "${it.key}==${it.value}" })
        }
        addBreadcrumb(e.message.toString(), mapOf(), BreadcrumbType.ERROR)

        notify(report)
    }

    fun notify(report: BugsnagEvent) {
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
