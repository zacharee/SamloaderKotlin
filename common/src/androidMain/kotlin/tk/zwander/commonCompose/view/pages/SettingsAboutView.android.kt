package tk.zwander.commonCompose.view.pages

import tk.zwander.common.util.hasExternalStorage
import tk.zwander.common.util.requestExternalStorage
import tk.zwander.samsungfirmwaredownloader.App

actual object PlatformSettingsActions {
    actual fun androidHasStoragePermission(): Boolean {
        return App.instance.hasExternalStorage
    }

    actual fun androidRequestStoragePermission() {
        if (!androidHasStoragePermission()) {
            App.instance.activeActivity?.requestExternalStorage()
        }
    }
}