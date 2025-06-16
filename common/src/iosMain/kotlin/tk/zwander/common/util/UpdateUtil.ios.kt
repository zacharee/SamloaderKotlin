package tk.zwander.common.util

actual object UpdateUtil {
    actual suspend fun checkForUpdate(): UpdateInfo? {
        return null
    }

    actual suspend fun installUpdate() {
    }
}