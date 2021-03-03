package tk.zwander.common.util

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

fun makeFirmwareString(pda: String, csc: String): String {
    return "$pda/$csc/$pda/$pda"
}

suspend fun getFirmwareHistoryString(model: String, region: String): String? {
    val origUrl = "https://www.sammobile.com/firmwares/database/${model}/${region}"
    val client = HttpClient {
        followRedirects = true
        expectSuccess = false
    }

    val response = client.get<HttpResponse> {
        url(origUrl)
    }

    return if (response.status.isSuccess()) response.readText() else null
}