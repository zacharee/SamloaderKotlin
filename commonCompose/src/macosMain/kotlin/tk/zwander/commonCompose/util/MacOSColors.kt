package tk.zwander.commonCompose.util

import androidx.compose.ui.graphics.Color

// https://github.com/weisJ/darklaf/blob/cde54833f77b1f4cef8c713c3cd416dfaf87b897/macos/src/main/java/com/github/weisj/darklaf/platform/macos/theme/MacOSColors.java

// 0.000000 0.478431 1.000000
val ACCENT_BLUE: Color = color(0.000000f, 0.478431f, 1.000000f)

// 0.584314 0.239216 0.588235
val ACCENT_LILAC: Color = color(0.584314f, 0.239216f, 0.588235f)

// 0.968627 0.309804 0.619608
val ACCENT_ROSE: Color = color(0.968627f, 0.309804f, 0.619608f)

// 0.878431 0.219608 0.243137
val ACCENT_RED: Color = color(0.878431f, 0.219608f, 0.243137f)

// 0.968627 0.509804 0.105882
val ACCENT_ORANGE: Color = color(0.968627f, 0.509804f, 0.105882f)

// 0.988235 0.721569 0.152941
val ACCENT_YELLOW: Color = color(0.988235f, 0.721569f, 0.152941f)

// 0.384314 0.729412 0.274510
val ACCENT_GREEN: Color = color(0.384314f, 0.729412f, 0.274510f)

// 0.596078 0.596078 0.596078
val ACCENT_GRAPHITE: Color = color(0.596078f, 0.596078f, 0.596078f)

// 0.701961 0.843137 1.000000
val SELECTION_BLUE: Color = color(0.701961f, 0.843137f, 1.000000f)

// 0.874510 0.772549 0.874510
val SELECTION_PURPLE: Color = color(0.874510f, 0.772549f, 0.874510f)

// 0.988235 0.792157 0.886275
val SELECTION_PINK: Color = color(0.988235f, 0.792157f, 0.886275f)

// 0.960784 0.764706 0.772549
val SELECTION_RED: Color = color(0.960784f, 0.764706f, 0.772549f)

// 0.988235 0.850980 0.733333
val SELECTION_ORANGE: Color = color(0.988235f, 0.850980f, 0.733333f)

// 0.996078 0.913725 0.745098
val SELECTION_YELLOW: Color = color(0.996078f, 0.913725f, 0.745098f)

// 0.815686 0.917647 0.780392
val SELECTION_GREEN: Color = color(0.815686f, 0.917647f, 0.780392f)

// 0.878431 0.878431 0.878431
val SELECTION_GRAPHITE: Color = color(0.878431f, 0.878431f, 0.878431f)

private fun color(r: Float, g: Float, b: Float): Color {
    /*
         * For consistency with the native code we mirror the implementation of the float to int conversion
         * of the Color class.
         */
    return Color((r * 255 + 0.5).toInt(), (g * 255 + 0.5).toInt(), (b * 255 + 0.5).toInt())
}