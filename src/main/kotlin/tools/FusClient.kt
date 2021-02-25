package tools

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import java.net.http.HttpClient

class FusClient(
    var auth: String = "",
    var sessId: String = ""
) {
    var encNonce = ""
    var nonce = ""

    private val client = HttpClient.newBuilder()
        .build()

    init {
        makeReq("NF_DownloadGenerateNonce.do")
    }

    fun makeReq(path: String, data: String = ""): String {
        val authV = "FUS nonce=\"\", signature=\"${this.auth}\", nc=\"\", type=\"\", realm=\"\", newauth=\"1\""

        val (request, response, result) = "https://neofussvr.sslcs.cdngc.net/${path}"
            .httpPost()
            .body(data)
            .set("Authorization", authV)
            .set("User-Agent", "Kies2.0_FUS")
            .set("Cookie", "JSESSIONID=${sessId}")
            .set("Set-Cookie", "JSESSIONID=${sessId}")
            .responseString()

        if (response.headers.containsKey("NONCE")) {
            encNonce = response.headers["NONCE"].first()
            nonce = Auth.decryptNonce(encNonce)
            auth = Auth.getAuth(nonce)
        }

        if (response.headers.containsKey("Set-Cookie")) {
            sessId = response.headers["Set-Cookie"].firstOrNull { it.contains("JSESSIONID") }?.replace("JSESSIONID=", "")
                ?.replace(Regex(";.*$"), "") ?: sessId
        }

        return response.body().asString(response.headers[Headers.CONTENT_TYPE].lastOrNull())
    }

    fun downloadFile(fileName: String, start: Long = 0): Response {
        val authV = "FUS nonce=\"${encNonce}\", signature=\"${this.auth}\", nc=\"\", type=\"\", realm=\"\", newauth=\"1\""

        val (request, response, result) = "http://cloud-neofussvr.sslcs.cdngc.net/NF_DownloadBinaryForMass.do?file=${fileName}"
            .httpGet()
            .header("Authorization", authV)
            .header("User-Agent", "Kies2.0_FUS")
            .apply {
                if (start > 0) {
                    header("Range", "bytes=$start")
                }
            }
            .responseString()

        return response
    }
}