package tk.zwander.samsungfirmwaredownloader

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import tk.zwander.common.GradleConfig
import tk.zwander.common.util.CrossPlatformBugsnag
import java.util.concurrent.ConcurrentLinkedDeque

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: App
    }

    val activities = ConcurrentLinkedDeque<Activity>()
    val activeActivity: Activity?
        get() = activities.lastOrNull()

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

        registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?,
                ) {
                    activities.add(activity)
                }

                override fun onActivityDestroyed(activity: Activity) {
                    activities.remove(activity)
                }

                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle,
                ) {}
            },
        )
    }
}