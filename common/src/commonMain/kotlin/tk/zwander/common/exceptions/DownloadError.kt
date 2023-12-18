package tk.zwander.common.exceptions

class DownloadError(
    request: String,
    response: String,
    cause: Throwable? = null,
) : Exception(
    mapOf(
        "request" to request,
        "response" to response,
    ).toString(),
    cause,
)
