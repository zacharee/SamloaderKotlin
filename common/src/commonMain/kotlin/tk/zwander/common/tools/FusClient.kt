@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")

package tk.zwander.common.tools

import com.fleeksoft.io.exception.ArrayIndexOutOfBoundsException
import com.fleeksoft.ksoup.Ksoup
import com.linroid.ketch.api.Destination
import com.linroid.ketch.api.DownloadRequest
import com.linroid.ketch.api.DownloadState
import com.linroid.ketch.api.KetchError
import dev.zwander.kmp.platform.HostOS
import dev.zwander.kotlin.file.IPlatformFile
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.timeout
import io.ktor.client.request.headers
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.readTo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.io.InternalIoApi
import tk.zwander.common.data.BinaryFileInfo
import tk.zwander.common.util.BreadcrumbType
import tk.zwander.common.util.BugsnagUtils
import tk.zwander.common.util.firstElementByTagName
import tk.zwander.common.util.globalHttpClient
import tk.zwander.common.util.ketch
import tk.zwander.common.util.trackOperationProgress

/**
 * Manage communications with Samsung's server.
 */
object FusClient {
    enum class Request(val value: String, val cloud: Boolean) {
        GENERATE_NONCE("NF_SmartDownloadGenerateNonce.do", false),
        BINARY_INFORM("NF_SmartDownloadBinaryInform.do", false),
        BINARY_INIT("NF_SmartDownloadBinaryInitForMass.do", false)
    }

    private var encNonce = ""
    private var nonce = ""

    private var auth: String = ""
    private var sessionId: String = ""

    suspend fun getNonce(): String {
        if (nonce.isBlank()) {
            generateNonce()
        }

        return nonce
    }

    private suspend fun generateNonce() {
        BugsnagUtils.addBreadcrumb(
            message = "Generating nonce.",
            data = mapOf(),
            type = BreadcrumbType.LOG,
        )
        println("Generating nonce.")
        makeReq(Request.GENERATE_NONCE)
        BugsnagUtils.addBreadcrumb(
            message = "Nonce: $nonce, Auth: $auth",
            data = mapOf(),
            type = BreadcrumbType.LOG,
        )
        println("Nonce: $nonce")
        println("Auth: $auth")
    }

    private suspend fun makeSignatureHash(signature: String?): String? {
        if (signature == null) return null

        val hasher = CryptUtils.md5Provider.hasher()
        val a = hasher.hash("auth:$nonce:00000001".toByteArray()).toHexString()
        val b = hasher.hash("interface:$signature".toByteArray()).toHexString()

        return hasher.hash("$a:FUS:$b".toByteArray()).toHexString()
    }

    private suspend fun getAuthV(includeNonce: Boolean = true, signature: String? = null, cloud: Boolean = false): String {
        val hasSignature = !signature.isNullOrBlank()
        val nonce = when {
            includeNonce && hasSignature -> {
                val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
                CharArray(16) { chars.random() }.joinToString("")
            }
            includeNonce -> encNonce
            else -> ""
        }
        return "FUS nonce=\"${if (cloud) nonce else ""}\", signature=\"${makeSignatureHash(signature?.takeIf { !it.isBlank() }) ?: this.auth}\", nc=\"${if (hasSignature) "00000001" else ""}\", type=\"${if (hasSignature) "auth" else ""}\", realm=\"${if (hasSignature) "auth" else ""}\""
    }

    private fun getDownloadUrl(path: String): String {
        return "http://cloud-neofussvr.samsungmobile.com/NF_SmartDownloadBinaryForMass.do?file=${path}"
    }

    /**
     * Make a request to Samsung, automatically inserting authorization data.
     * @param request the request to make.
     * @param data any body data that needs to go into the request.
     * @return the response body data, as text. Usually XML.
     */
    suspend fun makeReq(request: Request, data: String = ""): String {
        if (nonce.isBlank() && request != Request.GENERATE_NONCE) {
            generateNonce()
        }

        val authV = getAuthV(cloud = request.cloud)

        val response =
            globalHttpClient.request("https://neofussvr.sslcs.cdngc.net/${request.value}") {
                method = HttpMethod.Post
                headers {
                    append("Authorization", authV)
                    append("User-Agent", "SMART 2.0")
                    append("Cookie", "JSESSIONID=${sessionId}")
                    append("Set-Cookie", "JSESSIONID=${sessionId}")
                    append(HttpHeaders.ContentLength, "${data.toByteArray().size}")
                }
                setBody(data)
            }

        val body = response.bodyAsText()

        if (request != Request.GENERATE_NONCE && response.is401(body)) {
            generateNonce()

            return makeReq(request, data)
        }

        if (response.headers["NONCE"] != null || response.headers["nonce"] != null) {
            try {
                encNonce = response.headers["NONCE"] ?: response.headers["nonce"] ?: ""
                nonce = encNonce

                try {
                    auth = CryptUtils.decryptNonce(encNonce.take(16).padEnd((16 - encNonce.length).coerceAtLeast(0), '0'))
                } catch (_: Exception) {}
            } catch (e: ArrayIndexOutOfBoundsException) {
                BugsnagUtils.addBreadcrumb(
                    message = "Error generating nonce.",
                    data = mapOf("error" to e),
                    type = BreadcrumbType.ERROR,
                )
                println("Error generating nonce.")
                e.printStackTrace()
            }
        }

        if (response.headers["Set-Cookie"] != null || response.headers["set-cookie"] != null) {
            sessionId = response.headers.entries()
                .find { it.value.any { value -> value.contains("JSESSIONID=") } }
                ?.value?.find {
                    it.contains("JSESSIONID=")
                }
                ?.replace("JSESSIONID=", "")
                ?.replace(Regex(";.*$"), "") ?: sessionId
        }

        return body
    }

    /**
     * Download a file from Samsung's server.
     * @param fileName the name of the file to download.
     * @param start an optional offset. Used for resuming downloads.
     */
    @OptIn(InternalAPI::class, InternalIoApi::class)
    suspend fun downloadFile(
        fileName: String,
        start: Long = 0,
        size: Long,
        dest: IPlatformFile,
        progressCallback: suspend (current: Long, max: Long, bps: Long) -> Unit,
    ): String? {
        val authV = getAuthV(cloud = true)
        val url = getDownloadUrl(fileName)

        return if (HostOS.current != HostOS.Android) {
            val task = ketch.tasks.value.find { it.request.url == url }
                ?.let { download ->
                    download.resume(Destination(dest.getAbsolutePath()))
                    download.takeIf {
                        it.state.value !is DownloadState.Completed
                    }
                } ?: ketch.download(
                DownloadRequest(
                    url = url,
                    destination = Destination(dest.getAbsolutePath()),
                    headers = mapOf(
                        "Authorization" to authV.also { println(it) },
                        "User-Agent" to "SMART 2.0",
                        "Cache-Control" to "no-cache",
                    ),
                ),
            )

            CoroutineScope(currentCoroutineContext()).launch(Dispatchers.IO) {
                task.state.collect {
                    if (it is DownloadState.Downloading) {
                        progressCallback(
                            it.progress.downloadedBytes,
                            size,
                            it.progress.bytesPerSecond,
                        )
                    }
                }
            }

            try {
                while (true) {
                    val result = task.await()

                    if (result.isSuccess) {
                        break
                    }

                    (result.exceptionOrNull() as? KetchError)?.let { error ->
                        if (!error.isRetryable) {
                            throw error
                        }
                    }
                }
            } catch (_: CancellationException) {
                task.pause()
            }

            null
        } else {
            val outputStream = dest.openOutputStream(true) ?: return null

            val request = globalHttpClient.prepareRequest {
                method = HttpMethod.Get
                url(url)
                headers {
                    append("Authorization", authV)
                    append("User-Agent", "SMART 2.0")
                    if (start > 0) {
                        append("Range", "bytes=${start}-")
                    }
                }
                timeout {
                    this.requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                    this.socketTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                    this.connectTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                }
            }

            try {
                request.execute { response ->
                    val md5 = response.headers["Content-MD5"]
                    val channel = response.bodyAsChannel()

                    trackOperationProgress(
                        size = size,
                        progressCallback = progressCallback,
                        operation = {
                            channel.readTo(outputStream, 8192L)
                        },
                        progressOffset = start,
                        condition = { !channel.isClosedForRead },
                        throttle = false,
                    )

                    outputStream.flush()

                    md5
                }
            } finally {
                outputStream.flush()
                outputStream.close()
            }
        }

    }

    private fun HttpResponse.is401(body: String): Boolean {
        if (status.value == 401) {
            return true
        }

        try {
            val xml = Ksoup.parse(body)

            val status = xml.firstElementByTagName("FUSBody")
                ?.firstElementByTagName("Results")
                ?.firstElementByTagName("Status")
                ?.text()

            if (status == "401") {
                return true
            }
        } catch (_: Throwable) {
        }

        return false
    }
}
