package tk.zwander.common.util

/**
 * Currently, desktop scaling with Jetpack Compose is a little broken.
 * This is used to set an extra scaling modifier on desktop platforms
 * so the UI displays properly.
 */
expect object DPScale {
    val dpScale: Float
}