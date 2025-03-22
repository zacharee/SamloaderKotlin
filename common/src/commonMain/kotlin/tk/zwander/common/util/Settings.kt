package tk.zwander.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

expect fun ObservableSettings(): ObservableSettings

class ObservableBifrostSettings(private val wrapped: ObservableSettings) :
    ObservableSettings by wrapped {
    override fun putString(key: String, value: String) {
        try {
            val newValue = value.replace("\u0000", "")
            val truncatedValue = newValue.take(8192)

            wrapped.putString(
                key,
                truncatedValue,
            )
        } catch (e: Throwable) {
            throw IllegalArgumentException("Error saving value for $key", e)
        }
    }
}

object BifrostSettings {
    object Keys {
        val useMicaEffect = SettingsKey.Boolean("useMicaEffect", false, settings, disabled = true)
        val useVibrancyEffect = SettingsKey.Boolean("useVibrancyEffect", false, settings)
        val allowLowercaseCharacters =
            SettingsKey.Boolean("allowLowercaseCharacters", false, settings)
        val autoDeleteEncryptedFirmware =
            SettingsKey.Boolean("autoDeleteEncryptedFirmware", false, settings)
        val bugsnagUuid = SettingsKey.String<String?>("bugsnag_user_id", null, settings)
        val enableDecryptKeySave = SettingsKey.Boolean("enable_decryption_key_download", false, settings)
    }

    val settings = ObservableBifrostSettings(ObservableSettings())

    val useTransparencyEffects: Flow<Boolean>
        get() = combine(Keys.useMicaEffect.asMutableStateFlow(), Keys.useVibrancyEffect.asMutableStateFlow()) { m, v -> m || v }
}

@Suppress("unused", "UNCHECKED_CAST")
sealed class SettingsKey<Type : Any?> {
    abstract val key: kotlin.String
    abstract val default: Type
    abstract val settings: ObservableSettings

    abstract fun getValue(): Type
    abstract fun setValue(value: Type)

    protected abstract fun registerListener(callback: (Type?) -> Unit): SettingsListener

    @OptIn(DelicateCoroutinesApi::class, ExperimentalForInheritanceCoroutinesApi::class)
    fun asMutableStateFlow(): MutableStateFlow<Type> {
        val wrappedFlow = MutableStateFlow(getValue())
        val flow = object : MutableStateFlow<Type> by wrappedFlow {
            override var value: Type
                get() = wrappedFlow.value
                set(value) {
                    wrappedFlow.value = value
                    GlobalScope.launch(Dispatchers.IO) {
                        setValue(value)
                    }
                }

            override suspend fun emit(value: Type) {
                wrappedFlow.emit(value)
                GlobalScope.launch(Dispatchers.IO) {
                    setValue(value)
                }
            }

            override fun tryEmit(value: Type): kotlin.Boolean {
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
            flow.value = it ?: default
        }

        return flow
    }

    @Composable
    fun collectAsMutableState(): MutableState<Type> {
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

    operator fun invoke(): Type = getValue()
    operator fun invoke(value: Type) = setValue(value)

    fun getAndSetDefaultIfNonExistent(defValue: Type): Type {
        return if (settings.hasKey(key)) {
            getValue()
        } else {
            setValue(defValue)
            defValue
        }
    }

    data class Boolean<T : kotlin.Boolean?>(
        override val key: kotlin.String,
        override val default: T,
        override val settings: ObservableSettings,
        val disabled: kotlin.Boolean = false,
    ) : SettingsKey<T>() {
        override fun getValue(): T {
            return if (disabled) default else (settings.getBooleanOrNull(key) ?: default) as T
        }

        override fun setValue(value: T) {
            if (value == null) {
                settings.remove(key)
            } else {
                settings.putBoolean(key, value)
            }
        }

        override fun registerListener(callback: (T?) -> Unit): SettingsListener {
            return settings.addBooleanOrNullListener(key, callback as (kotlin.Boolean?) -> Unit)
        }
    }

    data class Int<T : kotlin.Int?>(
        override val key: kotlin.String,
        override val default: T,
        override val settings: ObservableSettings,
    ) : SettingsKey<T>() {
        override fun getValue(): T {
            return (settings.getIntOrNull(key) ?: default) as T
        }

        override fun setValue(value: T) {
            if (value == null) {
                settings.remove(key)
            } else {
                settings.putInt(key, value)
            }
        }

        override fun registerListener(callback: (T?) -> Unit): SettingsListener {
            return settings.addIntOrNullListener(key, callback as (kotlin.Int?) -> Unit)
        }
    }

    data class Long<T : kotlin.Long?>(
        override val key: kotlin.String,
        override val default: T,
        override val settings: ObservableSettings,
    ) : SettingsKey<T>() {
        override fun getValue(): T {
            return (settings.getLongOrNull(key) ?: default) as T
        }

        override fun setValue(value: T) {
            if (value == null) {
                settings.remove(key)
            } else {
                settings.putLong(key, value)
            }
        }

        override fun registerListener(callback: (T?) -> Unit): SettingsListener {
            return settings.addLongOrNullListener(key, callback as (kotlin.Long?) -> Unit)
        }
    }

    data class String<T : kotlin.String?>(
        override val key: kotlin.String,
        override val default: T,
        override val settings: ObservableSettings,
    ) : SettingsKey<T>() {
        override fun getValue(): T {
            return (settings.getStringOrNull(key) ?: default) as T
        }

        override fun setValue(value: T) {
            if (value == null) {
                settings.remove(key)
            } else {
                settings.putString(key, value)
            }
        }

        override fun registerListener(callback: (T?) -> Unit): SettingsListener {
            return settings.addStringOrNullListener(key, callback as (kotlin.String?) -> Unit)
        }
    }

    data class Float<T : kotlin.Float?>(
        override val key: kotlin.String,
        override val default: T,
        override val settings: ObservableSettings,
    ) : SettingsKey<T>() {
        override fun getValue(): T {
            return (settings.getFloatOrNull(key) ?: default) as T
        }

        override fun setValue(value: T) {
            if (value == null) {
                settings.remove(key)
            } else {
                settings.putFloat(key, value)
            }
        }

        override fun registerListener(callback: (T?) -> Unit): SettingsListener {
            return settings.addFloatOrNullListener(key, callback as (kotlin.Float?) -> Unit)
        }
    }

    data class Double<T : kotlin.Double?>(
        override val key: kotlin.String,
        override val default: T,
        override val settings: ObservableSettings,
    ) : SettingsKey<T>() {
        override fun getValue(): T {
            return (settings.getDoubleOrNull(key) ?: default) as T
        }

        override fun setValue(value: T) {
            if (value == null) {
                settings.remove(key)
            } else {
                settings.putDouble(key, value)
            }
        }

        override fun registerListener(callback: (T?) -> Unit): SettingsListener {
            return settings.addDoubleOrNullListener(key, callback as (kotlin.Double?) -> Unit)
        }
    }

    data class Complex<Type>(
        override val key: kotlin.String,
        override val default: Type,
        val deserializer: (kotlin.String?) -> Type,
        val serializer: (Type) -> kotlin.String?,
        override val settings: ObservableSettings,
    ) : SettingsKey<Type>() {
        override fun getValue(): Type {
            return deserializer(settings.getStringOrNull(key))
        }

        override fun setValue(value: Type) {
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
