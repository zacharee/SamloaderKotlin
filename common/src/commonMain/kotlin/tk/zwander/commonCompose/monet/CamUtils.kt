package tk.zwander.commonCompose.monet

import androidx.compose.ui.graphics.Color
import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.round


/**
 * Collection of methods for transforming between color spaces.
 *
 *
 * Methods are named $xFrom$Y. For example, lstarFromInt() returns L* from an ARGB integer.
 *
 *
 * These methods, generally, convert colors between the L*a*b*, XYZ, and sRGB spaces.
 *
 *
 * L*a*b* is a perceptually accurate color space. This is particularly important in the L*
 * dimension: it measures luminance and unlike lightness measures traditionally used in UI work via
 * RGB or HSL, this luminance transitions smoothly, permitting creation of pleasing shades of a
 * color, and more pleasing transitions between colors.
 *
 *
 * XYZ is commonly used as an intermediate color space for converting between one color space to
 * another. For example, to convert RGB to L*a*b*, first RGB is converted to XYZ, then XYZ is
 * convered to L*a*b*.
 *
 *
 * sRGB is a "specification originated from work in 1990s through cooperation by Hewlett-Packard
 * and Microsoft, and it was designed to be a standard definition of RGB for the internet, which it
 * indeed became...The standard is based on a sampling of computer monitors at the time...The whole
 * idea of sRGB is that if everyone assumed that RGB meant the same thing, then the results would be
 * consistent, and reasonably good. It worked." - Fairchild, Color Models and Systems: Handbook of
 * Color Psychology, 2015
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object CamUtils {
    // Transforms XYZ color space coordinates to 'cone'/'RGB' responses in CAM16.
    val XYZ_TO_CAM16RGB = arrayOf(
        doubleArrayOf(0.401288, 0.650173, -0.051461),
        doubleArrayOf(-0.250268, 1.204414, 0.045854),
        doubleArrayOf(-0.002079, 0.048952, 0.953127)
    )

    // Transforms 'cone'/'RGB' responses in CAM16 to XYZ color space coordinates.
    val CAM16RGB_TO_XYZ = arrayOf(
        doubleArrayOf(1.86206786, -1.01125463, 0.14918677),
        doubleArrayOf(0.38752654, 0.62144744, -0.00897398),
        doubleArrayOf(-0.01584150, -0.03412294, 1.04996444)
    )

    // Need this, XYZ coordinates in internal ColorUtils are private
    // sRGB specification has D65 whitepoint - Stokes, Anderson, Chandrasekar, Motta - A Standard
    // Default Color Space for the Internet: sRGB, 1996
    val WHITE_POINT_D65 = doubleArrayOf(95.047, 100.0, 108.883)

    // This is a more precise sRGB to XYZ transformation matrix than traditionally
    // used. It was derived using Schlomer's technique of transforming the xyY
    // primaries to XYZ, then applying a correction to ensure mapping from sRGB
    // 1, 1, 1 to the reference white point, D65.
    private val SRGB_TO_XYZ = arrayOf(
        doubleArrayOf(0.41233895, 0.35762064, 0.18051042),
        doubleArrayOf(0.2126, 0.7152, 0.0722),
        doubleArrayOf(0.01932141, 0.11916382, 0.95034478)
    )
    private val XYZ_TO_SRGB = arrayOf(
        doubleArrayOf(
            3.2413774792388685, -1.5376652402851851, -0.49885366846268053
        ), doubleArrayOf(
            -0.9691452513005321, 1.8758853451067872, 0.04156585616912061
        ), doubleArrayOf(
            0.05562093689691305, -0.20395524564742123, 1.0571799111220335
        )
    )

    /**
     * The signum function.
     *
     * @return 1 if num > 0, -1 if num < 0, and 0 if num = 0
     */
    fun signum(num: Double): Int {
        return if (num < 0) {
            -1
        } else if (num == 0.0) {
            0
        } else {
            1
        }
    }

    /**
     * Converts an L* value to an ARGB representation.
     *
     * @param lstar L* in L*a*b*
     * @return ARGB representation of grayscale color with lightness matching L*
     */
    fun argbFromLstar(lstar: Double): Int {
        val fy = (lstar + 16.0) / 116.0
        val kappa = 24389.0 / 27.0
        val epsilon = 216.0 / 24389.0
        val lExceedsEpsilonKappa = lstar > 8.0
        val y = if (lExceedsEpsilonKappa) fy * fy * fy else lstar / kappa
        val cubeExceedEpsilon = fy * fy * fy > epsilon
        val x = if (cubeExceedEpsilon) fy * fy * fy else lstar / kappa
        val z = if (cubeExceedEpsilon) fy * fy * fy else lstar / kappa
        val whitePoint = WHITE_POINT_D65
        return argbFromXyz(x * whitePoint[0], y * whitePoint[1], z * whitePoint[2])
    }

    /** Converts a color from ARGB to XYZ.  */
    fun argbFromXyz(x: Double, y: Double, z: Double): Int {
        val matrix = XYZ_TO_SRGB
        val linearR = matrix[0][0] * x + matrix[0][1] * y + matrix[0][2] * z
        val linearG = matrix[1][0] * x + matrix[1][1] * y + matrix[1][2] * z
        val linearB = matrix[2][0] * x + matrix[2][1] * y + matrix[2][2] * z
        val r = delinearized(linearR)
        val g = delinearized(linearG)
        val b = delinearized(linearB)
        return argbFromRgb(r, g, b)
    }

    /** Converts a color from linear RGB components to ARGB format.  */
    fun argbFromLinrgb(linrgb: DoubleArray): Int {
        val r = delinearized(linrgb[0])
        val g = delinearized(linrgb[1])
        val b = delinearized(linrgb[2])
        return argbFromRgb(r, g, b)
    }

    /** Converts a color from linear RGB components to ARGB format.  */
    fun argbFromLinrgbComponents(r: Double, g: Double, b: Double): Int {
        return argbFromRgb(delinearized(r), delinearized(g), delinearized(b))
    }

    /**
     * Delinearizes an RGB component.
     *
     * @param rgbComponent 0.0 <= rgb_component <= 100.0, represents linear R/G/B channel
     * @return 0 <= output <= 255, color channel converted to regular RGB space
     */
    fun delinearized(rgbComponent: Double): Int {
        val normalized = rgbComponent / 100.0
        val delinearized = if (normalized <= 0.0031308) {
            normalized * 12.92
        } else {
            1.055 * normalized.pow(1.0 / 2.4) - 0.055
        }
        return clampInt(0, 255, round(delinearized * 255.0).toInt())
    }

    /**
     * Clamps an integer between two integers.
     *
     * @return input when min <= input <= max, and either min or max otherwise.
     */
    fun clampInt(min: Int, max: Int, input: Int): Int {
        if (input < min) {
            return min
        } else if (input > max) {
            return max
        }
        return input
    }

    /** Converts a color from RGB components to ARGB format.  */
    fun argbFromRgb(red: Int, green: Int, blue: Int): Int {
        return 255 shl 24 or (red and 255 shl 16) or (green and 255 shl 8) or (blue and 255)
    }

    fun intFromLstar(lstar: Double): Int {
        if (lstar < 1) {
            return -0x1000000
        } else if (lstar > 99) {
            return -0x1
        }

        // XYZ to LAB conversion routine, assume a and b are 0.
        val fy = (lstar + 16.0) / 116.0

        // fz = fx = fy because a and b are 0
        val kappa = 24389f / 27f
        val epsilon = 216f / 24389f
        val lExceedsEpsilonKappa = lstar > 8.0f
        val yT = if (lExceedsEpsilonKappa) fy * fy * fy else lstar / kappa
        val cubeExceedEpsilon = fy * fy * fy > epsilon
        val xT = if (cubeExceedEpsilon) fy * fy * fy else (116f * fy - 16f) / kappa
        val zT = if (cubeExceedEpsilon) fy * fy * fy else (116f * fy - 16f) / kappa
        return ColorUtils.XYZToColor(
            (xT * WHITE_POINT_D65[0]),
            (yT * WHITE_POINT_D65[1]), (zT * WHITE_POINT_D65[2])
        )
    }

    /** Returns L* from L*a*b*, perceptual luminance, from an ARGB integer (ColorInt).  */
    fun lstarFromInt(argb: Int): Double {
        return lstarFromY(yFromInt(argb))
    }

    fun lstarFromY(y: Double): Double {
        val yMutable = y / 100.0
        val e = 216.0 / 24389.0
        val yIntermediate = if (yMutable <= e) {
            return 24389.0 / 27.0 * yMutable
        } else {
            cbrt(yMutable)
        }
        return 116.0 * yIntermediate - 16.0
    }

    fun yFromInt(argb: Int): Double {
        val color = Color(argb)
        val r = linearized((color.red * 255).toInt())
        val g = linearized((color.green * 255).toInt())
        val b = linearized((color.blue * 255).toInt())
        val matrix = SRGB_TO_XYZ
        val y = r * matrix[1][0] + g * matrix[1][1] + b * matrix[1][2]
        return y
    }

    fun xyzFromInt(argb: Int): DoubleArray {
        val color = Color(argb)
        val r = linearized((color.red * 255).toInt())
        val g = linearized((color.green * 255).toInt())
        val b = linearized((color.blue * 255).toInt())
        val matrix = SRGB_TO_XYZ
        val x = r * matrix[0][0] + g * matrix[0][1] + b * matrix[0][2]
        val y = r * matrix[1][0] + g * matrix[1][1] + b * matrix[1][2]
        val z = r * matrix[2][0] + g * matrix[2][1] + b * matrix[2][2]
        return doubleArrayOf(x, y, z)
    }

    /**
     * Converts an L* value to a Y value.
     *
     *
     * L* in L*a*b* and Y in XYZ measure the same quantity, luminance.
     *
     *
     * L* measures perceptual luminance, a linear scale. Y in XYZ measures relative luminance, a
     * logarithmic scale.
     *
     * @param lstar L* in L*a*b*
     * @return Y in XYZ
     */
    fun yFromLstar(lstar: Double): Double {
        val ke = 8.0
        return if (lstar > ke) {
            ((lstar + 16.0) / 116.0).pow(3.0) * 100.0
        } else {
            lstar / (24389.0 / 27.0) * 100.0
        }
    }

    fun linearized(rgbComponent: Int): Double {
        val normalized = rgbComponent.toDouble() / 255.0
        return if (normalized <= 0.04045) {
            normalized / 12.92 * 100.0
        } else {
            ((normalized + 0.055) / 1.055).pow(2.4) * 100.0
        }
    }
}