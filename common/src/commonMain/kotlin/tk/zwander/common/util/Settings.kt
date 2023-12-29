package tk.zwander.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

expect fun ObservableSettings(): ObservableSettings

class ObservableBifrostSettings(private val wrapped: ObservableSettings) :
    ObservableSettings by wrapped {
    override fun putString(key: String, value: String) {
        wrapped.putString(
            key,
            value.replace("\u0000", ""),
        )
    }
}

object BifrostSettings {
    object Keys {
        val useNativeFileDialog = SettingsKey.Boolean("useNativeFileDialog", true, settings)
        val allowLowercaseCharacters =
            SettingsKey.Boolean("allowLowercaseCharacters", false, settings)
        val autoDeleteEncryptedFirmware =
            SettingsKey.Boolean("autoDeleteEncryptedFirmware", false, settings)
    }

    val settings = ObservableBifrostSettings(ObservableSettings())
}

sealed class SettingsKey<Type> {
    abstract val key: kotlin.String
    abstract val default: Type?
    abstract val settings: ObservableSettings

    abstract fun getValue(): Type?
    abstract fun setValue(value: Type?)

    protected abstract fun registerListener(callback: (Type?) -> Unit): SettingsListener

    fun asMutableStateFlow(): MutableStateFlow<Type?> {
        val wrappedFlow = MutableStateFlow(getValue())
        val flow = object : MutableStateFlow<Type?> by wrappedFlow {
            override var value: Type?
                get() = wrappedFlow.value
                set(value) {
                    wrappedFlow.value = value
                    GlobalScope.launch(Dispatchers.IO) {
                        setValue(value)
                    }
                }

            override suspend fun emit(value: Type?) {
                wrappedFlow.emit(value)
                GlobalScope.launch(Dispatchers.IO) {
                    setValue(value)
                }
            }

            override fun tryEmit(value: Type?): kotlin.Boolean {
                return wrappedFlow.tryEmit(value).also {
                    if (it) {
                        GlobalScope.launch(Dispatchers.IO) {
                            setValue(value)
                        }
                    }
                }
            }
        }

        registerListener {
            flow.value = it
        }

        return flow
    }

    @Composable
    fun collectAsMutableState(): MutableState<Type?> {
        val state = remember {
            mutableStateOf(getValue())
        }

        LaunchedEffect(state.value) {
            setValue(state.value)
        }

        DisposableEffect(this) {
            val listener = registerListener {
                state.value = getValue()
            }

            onDispose {
                listener.deactivate()
            }
        }

        return state
    }

    operator fun invoke(): Type? = getValue()

    data class Boolean(
        override val key: kotlin.String,
        override val default: kotlin.Boolean?,
        override val settings: ObservableSettings,
    ) : SettingsKey<kotlin.Boolean>() {
        override fun getValue(): kotlin.Boolean? {
            return settings.getBooleanOrNull(key) ?: default
        }

        override fun setValue(value: kotlin.Boolean?) {
            if (value == null) {
                settings.remove(key)
            } else {
                settings.putBoolean(key, value)
            }
        }

        override fun registerListener(callback: (kotlin.Boolean?) -> Unit): SettingsListener {
            return settings.addBooleanOrNullListener(key, callback)
        }
    }

    data class Int(
        override val key: kotlin.String,
        override val default: kotlin.Int?,
        override val settings: ObservableSettings,
    ) : SettingsKey<kotlin.Int>() {
        override fun getValue(): kotlin.Int? {
            return settings.getIntOrNull(key) ?: default
        }

        override fun setValue(value: kotlin.Int?) {
            if (value == null) {
                settings.remove(key)
            } else {
                settings.putInt(key, value)
            }
        }

        override fun registerListener(callback: (kotlin.Int?) -> Unit): SettingsListener {
            return settings.addIntOrNullListener(key, callback)
        }
    }

    data class Long(
        override val key: kotlin.String,
        override val default: kotlin.Long?,
        override val settings: ObservableSettings,
    ) : SettingsKey<kotlin.Long>() {
        override fun getValue(): kotlin.Long? {
            return settings.getLongOrNull(key) ?: default
        }

        override fun setValue(value: kotlin.Long?) {
            if (value == null) {
                settings.remove(key)
            } else {
                settings.putLong(key, value)
            }
        }

        override fun registerListener(callback: (kotlin.Long?) -> Unit): SettingsListener {
            return settings.addLongOrNullListener(key, callback)
        }
    }

    data class String(
        override val key: kotlin.String,
        override val default: kotlin.String?,
        override val settings: ObservableSettings,
    ) : SettingsKey<kotlin.String>() {
        override fun getValue(): kotlin.String? {
            return settings.getStringOrNull(key) ?: default
        }

        override fun setValue(value: kotlin.String?) {
            if (value == null) {
                settings.remove(key)
            } else {
                settings.putString(key, value)
            }
        }

        override fun registerListener(callback: (kotlin.String?) -> Unit): SettingsListener {
            return settings.addStringOrNullListener(key, callback)
        }
    }

    data class Float(
        override val key: kotlin.String,
        override val default: kotlin.Float?,
        override val settings: ObservableSettings,
    ) : SettingsKey<kotlin.Float>() {
        override fun getValue(): kotlin.Float? {
            return settings.getFloatOrNull(key) ?: default
        }

        override fun setValue(value: kotlin.Float?) {
            if (value == null) {
                settings.remove(key)
            } else {
                settings.putFloat(key, value)
            }
        }

        override fun registerListener(callback: (kotlin.Float?) -> Unit): SettingsListener {
            return settings.addFloatOrNullListener(key, callback)
        }
    }

    data class Double(
        override val key: kotlin.String,
        override val default: kotlin.Double,
        override val settings: ObservableSettings,
    ) : SettingsKey<kotlin.Double>() {
        override fun getValue(): kotlin.Double {
            return settings.getDouble(key, default)
        }

        override fun setValue(value: kotlin.Double?) {
            if (value == null) {
                settings.remove(key)
            } else {
                settings.putDouble(key, value)
            }
        }

        override fun registerListener(callback: (kotlin.Double?) -> Unit): SettingsListener {
            return settings.addDoubleOrNullListener(key, callback)
        }
    }

    data class Complex<Type>(
        override val key: kotlin.String,
        override val default: Type?,
        val deserializer: (kotlin.String?) -> Type?,
        val serializer: (Type?) -> kotlin.String?,
        override val settings: ObservableSettings,
    ) : SettingsKey<Type>() {
        override fun getValue(): Type? {
            return deserializer(settings.getStringOrNull(key))
        }

        override fun setValue(value: Type?) {
            val serialized = serializer(value)

            if (serialized == null) {
                settings.remove(key)
            } else {
                settings.putString(key, serialized)
            }
        }

        override fun registerListener(callback: (Type?) -> Unit): SettingsListener {
            return settings.addStringOrNullListener(key) {
                callback(deserializer(it))
            }
        }
    }
}
