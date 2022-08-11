package org.jsoup

import io.ktor.utils.io.errors.*

/**
 * Signals that a HTTP request resulted in a not OK HTTP response.
 */
class HttpStatusException constructor(message: String, val statusCode: Int, val url: String) :
    IOException("$message. Status=$statusCode, URL=[$url]")
