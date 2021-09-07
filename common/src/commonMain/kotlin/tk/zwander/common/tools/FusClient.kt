package tk.zwander.common.tools

import com.soywiz.korio.async.launch
import com.soywiz.korio.async.runBlockingNoJs
import com.soywiz.korio.net.http.Http
import com.soywiz.korio.stream.AsyncInputStream
import com.soywiz.korio.stream.openAsync
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.request
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.core.internal.*
import tk.zwander.common.util.client
import tk.zwander.common.util.generateProperUrl
import kotlin.time.ExperimentalTime

/**
 * Manage communications with Samsung's server.
 */
@DangerousInternalIoApi
@OptIn(ExperimentalTime::class)
class FusClient(
    var auth: String = "",
    var sessId: String = "",
    val useProxy: Boolean = false
) {
    enum class Request(val value: String) {
        GENERATE_NONCE("NF_DownloadGenerateNonce.do"),
        BINARY_INFORM("NF_DownloadBinaryInform.do"),
        BINARY_INIT("NF_DownloadBinaryInitForMass.do")
    }

    var encNonce = ""
    var nonce = ""

    /**
     * Make a request to Samsung, automatically inserting authorization data.
     * @param request the request to make.
     * @param data any body data that needs to go into the request.
     * @return the response body data, as text. Usually XML.
     */
    @OptIn(InternalAPI::class)
    suspend fun makeReq(request: Request, data: String = ""): String {
        if (nonce.isBlank() && request != Request.GENERATE_NONCE) {
            //We need a nonce first.
            println("Generating nonce.")
            makeReq(Request.GENERATE_NONCE)
            println("Nonce: $nonce")
        }

        val authV = "FUS nonce=\"\", signature=\"${this.auth}\", nc=\"\", type=\"\", realm=\"\", newauth=\"1\""

        val response = client.request<HttpResponse>(generateProperUrl(useProxy, "https://neofussvr.sslcs.cdngc.net:443/${request.value}")) {
            method = HttpMethod.Post
            headers {
                append("Authorization", authV)
                append("User-Agent", "Kies2.0_FUS")
                append("Cookie", "JSESSIONID=${sessId}")
                append("Set-Cookie", "JSESSIONID=${sessId}")
            }
            body = data
        }

        if (response.headers["NONCE"] != null || response.headers["nonce"] != null) {
            encNonce = response.headers["NONCE"] ?: response.headers["nonce"] ?: ""
            nonce = CryptUtils.decryptNonce(encNonce)
            auth = CryptUtils.getAuth(nonce)
        }

        if (response.headers["Set-Cookie"] != null || response.headers["set-cookie"] != null) {
            sessId = response.headers.entries().find { it.value.any { it.contains("JSESSIONID=") } }
                ?.value?.find {
                    it.contains("JSESSIONID=")
                }
                ?.replace("JSESSIONID=", "")
                ?.replace(Regex(";.*$"), "") ?: sessId
        }

        response.setCookie()

        client.close()

        return response.readText()
    }

    /**
     * Download a file from Samsung's server.
     * @param fileName the name of the file to download.
     * @param start an optional offset. Used for resuming downloads.
     */
    suspend fun downloadFile(fileName: String, start: Long = 0): Pair<AsyncInputStream, String?> {
        val authV =
            "FUS nonce=\"${encNonce}\", signature=\"${this.auth}\", nc=\"\", type=\"\", realm=\"\", newauth=\"1\""
        val url = generateProperUrl(useProxy, "http://cloud-neofussvr.sslcs.cdngc.net/NF_DownloadBinaryForMass.do")

        val client = com.soywiz.korio.net.http.HttpClient()

        val response = client.request(
            Http.Method.GET,
            "$url?file=${fileName}",
            headers = Http.Headers(
                "Authorization" to authV,
                "User-Agent" to "Kies2.0_FUS",
            ).run {
                if (start > 0) {
                    this.plus(Http.Headers("Range" to "bytes=$start-"))
                } else this
            }
        )

        return response.content to response.headers["Content-MD5"]
    }

    suspend fun downloadFileWithKtor(fileName: String, start: Long = 0, onDownloadCallback: ((Long, Long) -> Unit)): Pair<ByteArray, String?> {
        val authV =
            "FUS nonce=\"${encNonce}\", signature=\"${this.auth}\", nc=\"\", type=\"\", realm=\"\", newauth=\"1\""
        val url = generateProperUrl(useProxy, "http://cloud-neofussvr.sslcs.cdngc.net/NF_DownloadBinaryForMass.do")

        val response = client.request<HttpResponse>() {
            url(URLBuilder(url).apply {
                parameters.append("file", fileName)
                parameters.urlEncodingOption = UrlEncodingOption.NO_ENCODING
            }.build())
            method = HttpMethod.Get
            headers {
                append("Authorization", authV)
                append("User-Agent", "Kies2.0_FUS")

                if (start > 0) {
                    append("Range", "bytes=$start-")
                }
            }
            onDownload { bytesSentTotal, contentLength -> onDownloadCallback.let { it(bytesSentTotal, contentLength) } }
        }

        return response.receive<ByteArray>() to response.headers["Content-MD5"]
    }
}
