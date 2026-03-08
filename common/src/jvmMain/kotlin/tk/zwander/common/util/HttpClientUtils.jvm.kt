package tk.zwander.common.util

import com.linroid.ketch.sqlite.DriverFactory
import net.harawata.appdirs.AppDirsFactory
import tk.zwander.common.GradleConfig
import java.io.File

actual val ketchDb: DriverFactory
    get() = DriverFactory(
        dbPath = File(
            AppDirsFactory.getInstance()
                .getUserDataDir(GradleConfig.appName, null, "Zachary Wander"),
            "ketch.db",
        ).absolutePath,
    )