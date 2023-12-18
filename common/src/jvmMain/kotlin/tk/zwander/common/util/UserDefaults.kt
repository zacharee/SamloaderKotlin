package tk.zwander.common.util

import androidx.compose.ui.graphics.Color
import korlibs.memory.dyn.osx.NSClass
import korlibs.memory.dyn.osx.NSObject
import korlibs.memory.dyn.osx.NSString
import korlibs.memory.dyn.osx.msgSend

class UserDefaults(id: Long) : NSObject(id) {
    fun objectForKey(key: String): String? =
        id.msgSend("objectForKey:", NSString(key))
            .msgSend("description")
            .takeIf { it != 0L }
            ?.let { NSString(it).cString }

    fun getAccentColor(): Color {
        return when (objectForKey("AppleAccentColor")?.toIntOrNull()) {
            -2 -> MacOSColors.ACCENT_BLUE
            -1 -> MacOSColors.ACCENT_GRAPHITE
            0 -> MacOSColors.ACCENT_RED
            1 -> MacOSColors.ACCENT_ORANGE
            2 -> MacOSColors.ACCENT_YELLOW
            3 -> MacOSColors.ACCENT_GREEN
            4 -> MacOSColors.ACCENT_LILAC
            5 -> MacOSColors.ACCENT_ROSE
            else -> MacOSColors.ACCENT_BLUE
        }
    }

    companion object : NSClass("NSUserDefaults") {
        fun standardUserDefaults(): UserDefaults =
            UserDefaults(OBJ_CLASS.msgSend("standardUserDefaults"))
    }
}

/**
 * https://github.com/weisJ/darklaf/blob/master/macos/src/main/java/com/github/weisj/darklaf/platform/macos/theme/MacOSColors.java#L28
 */
object MacOSColors {
    // 0.000000 0.478431 1.000000
    val ACCENT_BLUE = color(0.000000f, 0.478431f, 1.000000f)

    // 0.584314 0.239216 0.588235
    val ACCENT_LILAC = color(0.584314f, 0.239216f, 0.588235f)

    // 0.968627 0.309804 0.619608
    val ACCENT_ROSE = color(0.968627f, 0.309804f, 0.619608f)

    // 0.878431 0.219608 0.243137
    val ACCENT_RED = color(0.878431f, 0.219608f, 0.243137f)

    // 0.968627 0.509804 0.105882
    val ACCENT_ORANGE = color(0.968627f, 0.509804f, 0.105882f)

    // 0.988235 0.721569 0.152941
    val ACCENT_YELLOW = color(0.988235f, 0.721569f, 0.152941f)

    // 0.384314 0.729412 0.274510
    val ACCENT_GREEN = color(0.384314f, 0.729412f, 0.274510f)

    // 0.596078 0.596078 0.596078
    val ACCENT_GRAPHITE = color(0.596078f, 0.596078f, 0.596078f)

    private fun color(r: Float, g: Float, b: Float): Color {
        /*
         * For consistency with the native code we mirror the implementation of the float to int conversion
         * of the Color class.
         */
        return Color(
            red = (r * 255 + 0.5).toInt(),
            green = (g * 255 + 0.5).toInt(),
            blue = (b * 255 + 0.5).toInt(),
            alpha = 255
        )
    }
}
