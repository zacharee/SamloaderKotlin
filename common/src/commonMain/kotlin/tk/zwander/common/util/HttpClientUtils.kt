@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")

package tk.zwander.common.util

import com.linroid.kdown.core.KDown
import com.linroid.kdown.core.log.LogLevel
import com.linroid.kdown.core.log.Logger
import com.linroid.kdown.engine.KtorHttpEngine
import com.linroid.kdown.sqlite.DriverFactory
import com.linroid.kdown.sqlite.createSqliteTaskStore
import dev.zwander.kotlin.file.IPlatformFile
import io.ktor.client.*
import io.ktor.client.plugins.HttpTimeout

val globalHttpClient: HttpClient
    get() = HttpClient {
        this.followRedirects = true
        this.expectSuccess = false

        install(HttpTimeout)
    }

val kdown = KDown(
    httpEngine = KtorHttpEngine(globalHttpClient),
    logger = Logger.console(minLevel = LogLevel.DEBUG),
    taskStore = createSqliteTaskStore(kdownDb),
)

expect val kdownDb: DriverFactory
