@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")

package tk.zwander.common.util

import com.linroid.ketch.api.DownloadConfig
import com.linroid.ketch.api.log.KetchLogger
import com.linroid.ketch.api.log.LogLevel
import com.linroid.ketch.api.log.Logger
import com.linroid.ketch.core.Ketch
import com.linroid.ketch.engine.KtorHttpEngine
import com.linroid.ketch.sqlite.DriverFactory
import com.linroid.ketch.sqlite.createSqliteTaskStore
import io.ktor.client.*
import io.ktor.client.plugins.HttpTimeout

val globalHttpClient: HttpClient
    get() = HttpClient {
        this.followRedirects = true
        this.expectSuccess = false

        install(HttpTimeout)
    }

val ketch: Ketch = Ketch(
    httpEngine = KtorHttpEngine(globalHttpClient),
    config = DownloadConfig(
        maxConnectionsPerDownload = 8,
    ),
    taskStore = createSqliteTaskStore(ketchDb),
    logger = Logger.console(minLevel = LogLevel.WARN),
)

expect val ketchDb: DriverFactory
