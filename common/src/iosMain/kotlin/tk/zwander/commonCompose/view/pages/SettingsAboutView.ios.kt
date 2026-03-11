package tk.zwander.commonCompose.view.pages

actual object PlatformSettingsActions {
    actual fun androidHasStoragePermission(): Boolean {
        return false
    }

    actual fun androidRequestStoragePermission() {}
}