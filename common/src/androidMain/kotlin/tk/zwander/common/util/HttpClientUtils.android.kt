package tk.zwander.common.util

import com.linroid.kdown.sqlite.DriverFactory
import tk.zwander.samsungfirmwaredownloader.App

actual val kdownDb: DriverFactory
    get() = DriverFactory(App.instance, "kdown.db")