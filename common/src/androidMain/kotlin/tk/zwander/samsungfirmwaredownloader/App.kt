package tk.zwander.samsungfirmwaredownloader

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import tk.zwander.common.GradleConfig
import tk.zwander.common.util.CrossPlatformBugsnag

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: App
    }

    override fun onCreate() {
        instance = this
        super.onCreate()

        Bugsnag.start(
            this,
            (Configuration::class.java
                .getDeclaredMethod("load", Context::class.java, String::class.java)
                .apply { isAccessible = true }
                .invoke(null, this, GradleConfig.bugsnagAndroidApiKey) as Configuration).apply {
                projectPackages = CrossPlatformBugsnag.appPackages.toSet()
            },
        )
    }
}