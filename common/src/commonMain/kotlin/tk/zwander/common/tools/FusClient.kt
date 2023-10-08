package tk.zwander.common.tools

import com.soywiz.korio.stream.AsyncInputStream
import io.ktor.client.request.*
import io.ktor.client.request.request
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import tk.zwander.common.util.client
import tk.zwander.common.util.generateProperUrl
import kotlin.time.ExperimentalTime

@OptIn(DangerousInternalIoApi::class)
expect suspend fun doDownloadFile(client: FusClient, fileName: String, start: Long = 0): Pair<AsyncInputStream, String?>

/**
 * Manage communications with Samsung's server.
 */
@DangerousInternalIoApi
@OptIn(ExperimentalTime::class)
class FusClient(
    private var auth: String = "",
    private var sessId: String = "",
    private val useProxy: Boolean = tk.zwander.common.util.useProxy
) {
    enum class Request(val value: String) {
        GENERATE_NONCE("NF_DownloadGenerateNonce.do"),
        BINARY_INFORM("NF_DownloadBinaryInform.do"),
        BINARY_INIT("NF_DownloadBinaryInitForMass.do")
    }

    private var encNonce = ""
    private var nonce = ""

    suspend fun getAuth(): String {
        if (auth.isBlank()) {
            generateNonce()
        }

        return auth
    }

    suspend fun getEncNonce(): String {
        if (encNonce.isBlank()) {
            generateNonce()
        }

        return encNonce
    }

    suspend fun getNonce(): String {
        if (nonce.isBlank()) {
            generateNonce()
        }

        return nonce
    }

    suspend fun generateNonce() {
        println("Generating nonce.")
        makeReq(Request.GENERATE_NONCE)
        println("Nonce: $nonce")
        println("Auth: $auth")
    }

    fun getAuthV(): String {
        return "FUS nonce=\"$encNonce\", signature=\"${this.auth}\", nc=\"\", type=\"\", realm=\"\", newauth=\"1\""
    }

    fun getDownloadUrl(path: String): String {
        return generateProperUrl(useProxy, "http://cloud-neofussvr.samsungmobile.com/NF_DownloadBinaryForMass.do?file=${path}")
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

        val authV = getAuthV()

        val response = client.use {
            it.request(generateProperUrl(useProxy, "https://neofussvr.sslcs.cdngc.net:443/${request.value}")) {
                method = HttpMethod.Post
                headers {
                    append("Authorization", authV)
                    append("User-Agent", "Kies2.0_FUS")
                    append("Cookie", "JSESSIONID=${sessId}")
                    append("Set-Cookie", "JSESSIONID=${sessId}")
                }
                setBody(data)
            }
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

        return response.bodyAsText()
    }

    /**
     * Download a file from Samsung's server.
     * @param fileName the name of the file to download.
     * @param start an optional offset. Used for resuming downloads.
     */
    suspend fun downloadFile(fileName: String, start: Long = 0): Pair<AsyncInputStream, String?> {
        return doDownloadFile(this, fileName, start)
    }
}
