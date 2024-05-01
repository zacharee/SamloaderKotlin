package tk.zwander.common.ui

import androidx.compose.ui.graphics.Color
import java.io.File

object LinuxAccentColorGetter {
    fun getAccentColor(): Color? {
        return try {
            val environmentValue = System.getenv()["XDG_SESSION_DESKTOP"]

            DESpecificGetter.findGetter(environmentValue)?.getAccentColor()
        } catch (e: Throwable) {
            e.printStackTrace()
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

    data object LXDE : DESpecificGetter("LXDE") {
        override fun getAccentColor(): Color? {
            val file = File("${System.getProperty("user.home")}/.config/lxsession/LXDE/desktop.conf")
            val line = file.useLines { lines ->
                lines.find { it.startsWith("sGtk/ColorScheme") }
            }

            if (line.isNullOrBlank()) {
                return null
            }

            val value = line.split("=").getOrNull(1) ?: return null
            val selectedBgColor = value.split("\\n").find { it.startsWith("selected_bg_color") } ?: return null

            val colorValue = selectedBgColor.split(":#").getOrNull(1) ?: return null
            val realColor = if (colorValue.length == 6) {
                colorValue
            } else {
                // LXDE sets a 12-character color with the 6-char color (sort of) interleaved.
                // It isn't always exactly accurate, but the format makes no sense, and this is close enough.
                "${colorValue.slice(0..1)}${colorValue.slice(4..5)}${colorValue.slice(8..9)}"
            }

            return try {
                Color(java.awt.Color.decode("#${realColor}").rgb)
            } catch (e: Exception) {
                null
            }
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
