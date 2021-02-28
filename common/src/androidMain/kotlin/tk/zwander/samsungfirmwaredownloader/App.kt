package tk.zwander.samsungfirmwaredownloader

import android.annotation.SuppressLint
import android.app.Application

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: App? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}