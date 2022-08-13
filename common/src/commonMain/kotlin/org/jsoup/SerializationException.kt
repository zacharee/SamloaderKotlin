package org.jsoup

/**
 * A SerializationException is raised whenever serialization of a DOM element fails. This exception usually wraps an
 * [java.io.IOException] that may be thrown due to an inaccessible output stream.
 */
class SerializationException : RuntimeException {
    /**
     * Creates and initializes a new serialization exception with no error message and cause.
     */
    constructor() : super()

    /**
     * Creates and initializes a new serialization exception with the given error message and no cause.
     *
     * @param message
     * the error message of the new serialization exception (may be `null`).
     */
    constructor(message: String?) : super(message)

    /**
     * Creates and initializes a new serialization exception with the specified cause and an error message of
     * `(cause==null ? null : cause.toString())` (which typically contains the class and error message of
     * `cause`).
     *
     * @param cause
     * the cause of the new serialization exception (may be `null`).
     */
    constructor(cause: Throwable?) : super(cause)

    /**
     * Creates and initializes a new serialization exception with the given error message and cause.
     *
     * @param message
     * the error message of the new serialization exception.
     * @param cause
     * the cause of the new serialization exception.
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
