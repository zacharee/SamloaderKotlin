package tk.zwander.common.util

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import platform.Foundation.NSUserDefaults

actual fun ObservableSettings(): ObservableSettings = NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
