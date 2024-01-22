package tk.zwander.common.util

import androidx.preference.PreferenceManager
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import tk.zwander.samsungfirmwaredownloader.App

actual fun ObservableSettings(): ObservableSettings = SharedPreferencesSettings(
    PreferenceManager.getDefaultSharedPreferences(App.instance)
)
