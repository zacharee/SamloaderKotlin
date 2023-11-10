package tk.zwander.common.tools

import io.ktor.utils.io.core.internal.*
import korlibs.io.net.http.Http
import korlibs.io.net.http.HttpClient
import korlibs.io.stream.AsyncInputStream

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
