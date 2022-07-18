package tk.zwander.common.tools

import com.soywiz.korio.net.http.Http
import com.soywiz.korio.net.http.HttpClient
import com.soywiz.korio.stream.AsyncInputStream
import io.ktor.utils.io.core.internal.*

@OptIn(DangerousInternalIoApi::class)
actual suspend fun doDownloadFile(client: FusClient, fileName: String, start: Long): Pair<AsyncInputStream, String?> {
    val authV = client.getAuthV()
    val url = client.getDownloadUrl(fileName)

    val httpClient = HttpClient()

    val response = httpClient.request(
        Http.Method.GET,
        url,
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
