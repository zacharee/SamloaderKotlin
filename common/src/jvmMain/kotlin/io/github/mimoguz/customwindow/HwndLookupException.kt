package io.github.mimoguz.customwindow

/**
 * When [WindowHandle.tryFind] method fails,
 * it throws this exception.
 */
@Suppress("unused")
class HwndLookupException(
    /**
     * Get the main cause of the exception.
     *
     * @return The error type
     */
    val error: Error
) : Exception() {

    enum class Error {
        /**
         * Current platform is not supported
         */
        NOT_SUPPORTED,

        /**
         * Couldn't find the window
         */
        NOT_FOUND,

        /**
         * titleProperty of the window is bound, can't perform the search operation.
         */
        BOUND
    }
}