package tk.zwander.commonCompose.monet

import kotlin.math.PI
import kotlin.math.cbrt
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * The frame, or viewing conditions, where a color was seen. Used, along with a color, to create a
 * color appearance model representing the color.
 *
 *
 * To convert a traditional color to a color appearance model, it requires knowing what
 * conditions the color was observed in. Our perception of color depends on, for example, the tone
 * of the light illuminating the color, how bright that light was, etc.
 *
 *
 * This class is modelled separately from the color appearance model itself because there are a
 * number of calculations during the color => CAM conversion process that depend only on the viewing
 * conditions. Caching those calculations in a Frame instance saves a significant amount of time.
 */
class Frame private constructor(
    val n: Double,
    val aw: Double,
    val nbb: Double,
    val ncb: Double,
    val c: Double,
    val nc: Double,
    val rgbD: DoubleArray,
    val fl: Double,
    val flRoot: Double,
    val z: Double
) {

    companion object {
        // Standard viewing conditions assumed in RGB specification - Stokes, Anderson, Chandrasekar,
        // Motta - A Standard Default Color Space for the Internet: sRGB, 1996.
        //
        // White point = D65
        // Luminance of adapting field: 200 / Pi / 5, units are cd/m^2.
        //   sRGB ambient illuminance = 64 lux (per sRGB spec). However, the spec notes this is
        //     artificially low and based on monitors in 1990s. Use 200, the sRGB spec says this is the
        //     real average, and a survey of lux values on Wikipedia confirms this is a comfortable
        //     default: somewhere between a very dark overcast day and office lighting.
        //   Per CAM16 introduction paper (Li et al, 2017) Ew = pi * lw, and La = lw * Yb/Yw
        //   Ew = ambient environment luminance, in lux.
        //   Yb/Yw is taken to be midgray, ~20% relative luminance (XYZ Y 18.4, CIELAB L* 50).
        //   Therefore La = (Ew / pi) * .184
        //   La = 200 / pi * .184
        // Image surround to 10 degrees = ~20% relative luminance = CIELAB L* 50
        //
        // Not from sRGB standard:
        // Surround = average, 2.0.
        // Discounting illuminant = false, doesn't occur for self-luminous displays
        val DEFAULT = make(
            CamUtils.WHITE_POINT_D65,
            (200.0 / PI * CamUtils.yFromLstar(50.0) / 100.0),
            50.0,
            2.0,
            false
        )

        /** Create a custom frame.  */
        fun make(
            whitepoint: DoubleArray, adaptingLuminance: Double,
            backgroundLstar: Double, surround: Double, discountingIlluminant: Boolean
        ): Frame {
            // Transform white point XYZ to 'cone'/'rgb' responses
            val matrix = CamUtils.XYZ_TO_CAM16RGB
            val rW =
                whitepoint[0] * matrix[0][0] + whitepoint[1] * matrix[0][1] + whitepoint[2] * matrix[0][2]
            val gW =
                whitepoint[0] * matrix[1][0] + whitepoint[1] * matrix[1][1] + whitepoint[2] * matrix[1][2]
            val bW =
                whitepoint[0] * matrix[2][0] + whitepoint[1] * matrix[2][1] + whitepoint[2] * matrix[2][2]

            // Scale input surround, domain (0, 2), to CAM16 surround, domain (0.8, 1.0)
            val f = 0.8 + surround / 10.0
            // "Exponential non-linearity"
            val c: Double = if (f >= 0.9) lerp(
                0.59, 0.69,
                (f - 0.9) * 10.0
            ) else lerp(
                0.525, 0.59, (f - 0.8) * 10.0
            )
            // Calculate degree of adaptation to illuminant
            var d =
                if (discountingIlluminant) 1.0 else f * (1.0 - 1.0f / 3.6 * exp(
                    ((-adaptingLuminance - 42.0) / 92.0)
                ))
            // Per Li et al, if D is greater than 1 or less than 0, set it to 1 or 0.
            d = if (d > 1.0) 1.0 else if (d < 0.0) 0.0 else d
            // Chromatic induction factor

            // Cone responses to the whitepoint, adjusted for illuminant discounting.
            //
            // Why use 100.0 instead of the white point's relative luminance?
            //
            // Some papers and implementations, for both CAM02 and CAM16, use the Y
            // value of the reference white instead of 100. Fairchild's Color Appearance
            // Models (3rd edition) notes that this is in error: it was included in the
            // CIE 2004a report on CIECAM02, but, later parts of the conversion process
            // account for scaling of appearance relative to the white point relative
            // luminance. This part should simply use 100 as luminance.
            val rgbD = doubleArrayOf(
                d * (100.0 / rW) + 1.0 - d, d * (100.0 / gW) + 1.0 - d,
                d * (100.0 / bW) + 1.0 - d
            )
            // Luminance-level adaptation factor
            val k = 1.0 / (5.0 * adaptingLuminance + 1.0)
            val k4 = k * k * k * k
            val k4F = 1.0 - k4
            val fl: Double = k4 * adaptingLuminance + 0.1 * k4F * k4F * cbrt(
                5.0 * adaptingLuminance
            )

            // Intermediate factor, ratio of background relative luminance to white relative luminance
            val n = CamUtils.yFromLstar(backgroundLstar) / whitepoint[1]

            // Base exponential nonlinearity
            // note Schlomer 2018 has a typo and uses 1.58, the correct factor is 1.48
            val z: Double = 1.48 + sqrt(n)

            // Luminance-level induction factors
            val nbb: Double = 0.725 / pow(n, 0.2)

            // Discounted cone responses to the white point, adjusted for post-chromatic
            // adaptation perceptual nonlinearities.
            val rgbAFactors = doubleArrayOf(
                pow(fl * rgbD[0] * rW / 100.0, 0.42),
                pow(fl * rgbD[1] * gW / 100.0, 0.42),
                pow(
                    fl * rgbD[2] * bW / 100.0, 0.42
                ),
            )
            val rgbA = doubleArrayOf(
                400.0 * rgbAFactors[0] / (rgbAFactors[0] + 27.13),
                400.0 * rgbAFactors[1] / (rgbAFactors[1] + 27.13),
                400.0 * rgbAFactors[2] / (rgbAFactors[2] + 27.13)
            )
            val aw = (2.0 * rgbA[0] + rgbA[1] + 0.05 * rgbA[2]) * nbb
            return Frame(
                n,
                aw,
                nbb,
                nbb,
                c,
                f,
                rgbD,
                fl,
                pow(fl, 0.25),
                z
            )
        }
    }
}