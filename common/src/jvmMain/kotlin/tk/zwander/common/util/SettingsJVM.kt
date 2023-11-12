package tk.zwander.common.util

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

actual fun ObservableSettings(): ObservableSettings = PreferencesSettings(Preferences.userRoot())
