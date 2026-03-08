package tk.zwander.common.util

import com.linroid.ketch.sqlite.DriverFactory
import tk.zwander.samsungfirmwaredownloader.App

actual val ketchDb: DriverFactory
    get() = DriverFactory(App.instance, "kdown.db")
