package tk.zwander.common.ui

import androidx.compose.ui.graphics.Color
import korlibs.io.annotations.Keep
import tk.zwander.common.util.CrossPlatformBugsnag

object LinuxAccentColorGetter {
    fun getAccentColor(): Color? {
        return try {
            val environmentValue = System.getenv()["XDG_SESSION_DESKTOP"]

            DESpecificGetter.findGetter(environmentValue)?.getAccentColor()
        } catch (e: Throwable) {
            e.printStackTrace()
            CrossPlatformBugsnag.notify(e)
            null
        }
    }
}

sealed class DESpecificGetter(val sessionValue: String) {
    companion object {
        fun findGetter(de: String?): DESpecificGetter? {
            return DESpecificGetter::class.sealedSubclasses.firstNotNullOfOrNull {
                val obj = it.objectInstance

                if (obj?.sessionValue?.lowercase() == de?.lowercase()) {
                    obj
                } else {
                    null
                }
            }
        }
    }

    protected val runtime: Runtime? by lazy { Runtime.getRuntime() }

    abstract fun getAccentColor(): Color?

    @Keep
    data object KDE : DESpecificGetter("KDE") {
        override fun getAccentColor(): Color? {
            val rgb = runtime?.getLinesFromCommand(arrayOf("kreadconfig5", "--key", "AccentColor", "--group", "General"))
                ?.firstOrNull()

            if (rgb.isNullOrBlank()) {
                return null
            }

            val (r, g, b) = rgb.split(",")

            return Color(r.toInt(), g.toInt(), b.toInt())
        }
    }
}

fun Runtime.getLinesFromCommand(command: Array<String>): List<String>? {
    val proc = exec(command)

    proc?.inputStream?.bufferedReader()?.use { input ->
        return input.readLines()
    }

    proc?.waitFor()

    return null
}
