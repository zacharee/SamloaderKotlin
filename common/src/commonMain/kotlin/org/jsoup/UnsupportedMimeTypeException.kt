package org.jsoup

import io.ktor.utils.io.errors.*

/**
 * Signals that a HTTP response returned a mime type that is not supported.
 */
class UnsupportedMimeTypeException constructor(message: String, val mimeType: String, val url: String) :
    IOException(message) {

    public override fun toString(): String {
        return super.toString() + ". Mimetype=" + mimeType + ", URL=" + url
    }
}
