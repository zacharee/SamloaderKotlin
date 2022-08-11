package org.jsoup

import io.ktor.utils.io.errors.*

class UncheckedIOException : RuntimeException {
    constructor(cause: IOException?) : super(cause) {}
    constructor(message: String) : super(IOException(message)) {}

    fun ioException(): IOException {
        return cause as IOException
    }
}
