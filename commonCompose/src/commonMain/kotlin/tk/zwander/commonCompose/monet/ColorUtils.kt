package tk.zwander.commonCompose.monet

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.soywiz.kmem.clamp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


object ColorUtils {
    fun XYZToColor(
        x: Double,
        y: Double,
        z: Double
    ): Int {
        var r = (x * 3.2406 + y * -1.5372 + z * -0.4986) / 100
        var g = (x * -0.9689 + y * 1.8758 + z * 0.0415) / 100
        var b = (x * 0.0557 + y * -0.2040 + z * 1.0570) / 100
        r = if (r > 0.0031308) 1.055 * r.pow(1 / 2.4) - 0.055 else 12.92 * r
        g = if (g > 0.0031308) 1.055 * g.pow(1 / 2.4) - 0.055 else 12.92 * g
        b = if (b > 0.0031308) 1.055 * b.pow(1 / 2.4) - 0.055 else 12.92 * b

        return Color(
            red = r.clamp(0.0, 1.0).toFloat(),
            green = g.clamp(0.0, 1.0).toFloat(),
            blue = b.clamp(0.0, 1.0).toFloat(),
        ).toArgb()
    }

    /**
     * Convert a color appearance model representation to an ARGB color.
     *
     * Note: the returned color may have a lower chroma than requested. Whether a chroma is
     * available depends on luminance. For example, there's no such thing as a high chroma light
     * red, due to the limitations of our eyes and/or physics. If the requested chroma is
     * unavailable, the highest possible chroma at the requested luminance is returned.
     *
     * @param hue hue, in degrees, in CAM coordinates
     * @param chroma chroma in CAM coordinates.
     * @param lstar perceptual luminance, L* in L*a*b*
     */
    fun CAMToColor(hue: Float, chroma: Float, lstar: Float): Int {
        return Cam.getInt(hue, chroma, lstar)
    }

    /**
     * Convert the ARGB color to its HSL (hue-saturation-lightness) components.
     *
     *  * outHsl[0] is Hue [0 .. 360)
     *  * outHsl[1] is Saturation [0...1]
     *  * outHsl[2] is Lightness [0...1]
     *
     *
     * @param color  the ARGB color to convert. The alpha component is ignored
     * @param outHsl 3-element array which holds the resulting HSL components
     */
    fun colorToHSL(color: Int, outHsl: FloatArray) {
        val c = Color(color)

        RGBToHSL(
            (c.red * 255).toInt(),
            (c.green * 255).toInt(),
            (c.blue * 255).toInt(),
            outHsl
        )
    }

    /**
     * Convert RGB components to HSL (hue-saturation-lightness).
     *
     *  * outHsl[0] is Hue [0 .. 360)
     *  * outHsl[1] is Saturation [0...1]
     *  * outHsl[2] is Lightness [0...1]
     *
     *
     * @param r      red component value [0..255]
     * @param g      green component value [0..255]
     * @param b      blue component value [0..255]
     * @param outHsl 3-element array which holds the resulting HSL components
     */
    fun RGBToHSL(
        r: Int,
        g: Int, 
        b: Int,
        outHsl: FloatArray
    ) {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f
        val max: Float = max(rf, max(gf, bf))
        val min: Float = min(rf, min(gf, bf))
        val deltaMaxMin = max - min
        var h: Float
        val s: Float
        val l = (max + min) / 2f
        if (max == min) {
            // Monochromatic
            s = 0f
            h = s
        } else {
            h = when (max) {
                rf -> {
                    (gf - bf) / deltaMaxMin % 6f
                }
                gf -> {
                    (bf - rf) / deltaMaxMin + 2f
                }
                else -> {
                    (rf - gf) / deltaMaxMin + 4f
                }
            }
            s = deltaMaxMin / (1f - abs(2f * l - 1f))
        }
        h = h * 60f % 360f
        if (h < 0) {
            h += 360f
        }
        outHsl[0] = h.clamp(0f, 360f)
        outHsl[1] = s.clamp(0f, 1f)
        outHsl[2] = l.clamp(0f, 1f)
    }

    /**
     * Set the alpha component of `color` to be `alpha`.
     */
    fun setAlphaComponent(
        color: Int,
        alpha: Int
    ): Int {
        if (alpha < 0 || alpha > 255) {
            throw IllegalArgumentException("alpha must be between 0 and 255.")
        }
        return color and 0x00ffffff or (alpha shl 24)
    }
}