package tk.zwander.samsungfirmwaredownloader

import android.annotation.SuppressLint
import android.app.Application
import com.bugsnag.android.Bugsnag
import tk.zwander.common.GradleConfig

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: App
    }

    override fun onCreate() {
        instance = this
        super.onCreate()

        Bugsnag.start(this, GradleConfig.bugsnagAndroidApiKey)
    }
}