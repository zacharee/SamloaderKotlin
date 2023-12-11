package tk.zwander.common.exceptions

class DownloadError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
