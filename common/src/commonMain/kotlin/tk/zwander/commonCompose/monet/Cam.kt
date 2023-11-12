package tk.zwander.commonCompose.monet

import tk.zwander.commonCompose.monet.CamUtils.signum
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A color appearance model, based on CAM16, extended to use L* as the lightness dimension, and
 * coupled to a gamut mapping algorithm. Creates a color system, enables a digital design system.
 */
class Cam internal constructor(
    /** Hue in CAM16  */
    // CAM16 color dimensions, see getters for documentation.
    val hue: Float,
    /** Chroma in CAM16  */
    val chroma: Float, j: Float, q: Float, m: Float, s: Float, jstar: Float, astar: Float,
    bstar: Float
) {

    /** Lightness in CAM16  */
    val j: Float

    /**
     * Brightness in CAM16.
     *
     *
     * Prefer lightness, brightness is an absolute quantity. For example, a sheet of white paper
     * is much brighter viewed in sunlight than in indoor light, but it is the lightest object under
     * any lighting.
     */
    val q: Float

    /**
     * Colorfulness in CAM16.
     *
     *
     * Prefer chroma, colorfulness is an absolute quantity. For example, a yellow toy car is much
     * more colorful outside than inside, but it has the same chroma in both environments.
     */
    val m: Float

    /**
     * Saturation in CAM16.
     *
     *
     * Colorfulness in proportion to brightness. Prefer chroma, saturation measures colorfulness
     * relative to the color's own brightness, where chroma is colorfulness relative to white.
     */
    val s: Float

    /** Lightness coordinate in CAM16-UCS  */
    // Coordinates in UCS space. Used to determine color distance, like delta E equations in L*a*b*.
    val jstar: Float

    /** a* coordinate in CAM16-UCS  */
    val astar: Float

    /** b* coordinate in CAM16-UCS  */
    val bstar: Float

    /** Construct a CAM16 color  */
    init {
        this.j = j
        this.q = q
        this.m = m
        this.s = s
        this.jstar = jstar
        this.astar = astar
        this.bstar = bstar
    }

    /**
     * Distance in CAM16-UCS space between two colors.
     *
     *
     * Much like L*a*b* was designed to measure distance between colors, the CAM16 standard
     * defined a color space called CAM16-UCS to measure distance between CAM16 colors.
     */
    fun distance(other: Cam): Float {
        val dJ = jstar - other.jstar
        val dA = astar - other.astar
        val dB = bstar - other.bstar
        val dEPrime: Double = sqrt((dJ * dJ + dA * dA + dB * dB).toDouble())
        val dE: Double = 1.41 * dEPrime.pow(0.63)
        return dE.toFloat()
    }

    /** Returns perceived color as an ARGB integer, as viewed in standard sRGB frame.  */
    fun viewedInSrgb(): Int {
        return viewed(Frame.DEFAULT)
    }

    /** Returns color perceived in a frame as an ARGB integer.  */
    fun viewed(frame: Frame): Int {
        val alpha =
            if (chroma.toDouble() == 0.0 || j.toDouble() == 0.0) 0.0f else chroma / sqrt(
                j / 100.0
            ).toFloat()
        val t: Float = pow(
            alpha / pow(
                1.64 - pow(0.29, frame.n.toDouble()),
                0.73
            ),
            1.0 / 0.9
        ).toFloat()
        val hRad: Float = hue * PI.toFloat() / 180.0f
        val eHue: Float = 0.25f * (cos(hRad + 2.0) + 3.8).toFloat()
        val ac: Float = frame.aw * pow(
            j / 100.0,
            1.0 / frame.c / frame.z
        ).toFloat()
        val p1: Float = eHue * (50000.0f / 13.0f) * frame.nc * frame.ncb
        val p2: Float = ac / frame.nbb
        val hSin: Float = sin(hRad.toDouble()).toFloat()
        val hCos: Float = cos(hRad.toDouble()).toFloat()
        val gamma =
            23.0f * (p2 + 0.305f) * t / (23.0f * p1 + 11.0f * t * hCos + 108.0f * t * hSin)
        val a = gamma * hCos
        val b = gamma * hSin
        val rA = (460.0f * p2 + 451.0f * a + 288.0f * b) / 1403.0f
        val gA = (460.0f * p2 - 891.0f * a - 261.0f * b) / 1403.0f
        val bA = (460.0f * p2 - 220.0f * a - 6300.0f * b) / 1403.0f
        val rCBase: Float = max(
            0.0,
            27.13 * abs(rA) / (400.0 - abs(rA))
        ).toFloat()
        val rC: Float =
            signum(rA.toDouble()) * (100.0f / frame.fl) * pow(
                rCBase.toDouble(),
                1.0 / 0.42
            ).toFloat()
        val gCBase: Float = max(
            0.0,
            27.13 * abs(gA) / (400.0 - abs(gA))
        ).toFloat()
        val gC: Float =
            signum(gA.toDouble()) * (100.0f / frame.fl) * pow(
                gCBase.toDouble(),
                1.0 / 0.42
            ).toFloat()
        val bCBase: Float = max(
            0.0,
            27.13 * abs(bA) / (400.0 - abs(bA))
        ).toFloat()
        val bC: Float =
            signum(bA.toDouble()) * (100.0f / frame.fl) * pow(
                bCBase.toDouble(),
                1.0 / 0.42
            ).toFloat()
        val rF: Float = rC / frame.rgbD[0]
        val gF: Float = gC / frame.rgbD[1]
        val bF: Float = bC / frame.rgbD[2]
        val matrix: Array<FloatArray> =
            CamUtils.CAM16RGB_TO_XYZ
        val x =
            rF * matrix[0][0] + gF * matrix[0][1] + bF * matrix[0][2]
        val y =
            rF * matrix[1][0] + gF * matrix[1][1] + bF * matrix[1][2]
        val z =
            rF * matrix[2][0] + gF * matrix[2][1] + bF * matrix[2][2]
        return ColorUtils.XYZToColor(
            x.toDouble(),
            y.toDouble(),
            z.toDouble()
        )
    }

    companion object {
        // The maximum difference between the requested L* and the L* returned.
        private const val DL_MAX = 0.2f

        // The maximum color distance, in CAM16-UCS, between a requested color and the color returned.
        private const val DE_MAX = 1.0f

        // When the delta between the floor & ceiling of a binary search for chroma is less than this,
        // the binary search terminates.
        private const val CHROMA_SEARCH_ENDPOINT = 0.4f

        // When the delta between the floor & ceiling of a binary search for J, lightness in CAM16,
        // is less than this, the binary search terminates.
        private const val LIGHTNESS_SEARCH_ENDPOINT = 0.01f

        /**
         * Given a hue & chroma in CAM16, L* in L*a*b*, return an ARGB integer. The chroma of the color
         * returned may, and frequently will, be lower than requested. Assumes the color is viewed in
         * the
         * frame defined by the sRGB standard.
         */
        fun getInt(hue: Float, chroma: Float, lstar: Float): Int {
            return getInt(hue, chroma, lstar, Frame.DEFAULT)
        }

        /**
         * Create a color appearance model from a ARGB integer representing a color. It is assumed the
         * color was viewed in the frame defined in the sRGB standard.
         */
        fun fromInt(argb: Int): Cam {
            return fromIntInFrame(argb, Frame.DEFAULT)
        }

        /**
         * Create a color appearance model from a ARGB integer representing a color, specifying the
         * frame in which the color was viewed. Prefer Cam.fromInt.
         */
        fun fromIntInFrame(argb: Int, frame: Frame): Cam {
            // Transform ARGB int to XYZ
            val xyz: FloatArray = CamUtils.xyzFromInt(argb)

            // Transform XYZ to 'cone'/'rgb' responses
            val matrix: Array<FloatArray> =
                CamUtils.XYZ_TO_CAM16RGB
            val rT = xyz[0] * matrix[0][0] + xyz[1] * matrix[0][1] + xyz[2] * matrix[0][2]
            val gT = xyz[0] * matrix[1][0] + xyz[1] * matrix[1][1] + xyz[2] * matrix[1][2]
            val bT = xyz[0] * matrix[2][0] + xyz[1] * matrix[2][1] + xyz[2] * matrix[2][2]

            // Discount illuminant
            val rD: Float = frame.rgbD[0] * rT
            val gD: Float = frame.rgbD[1] * gT
            val bD: Float = frame.rgbD[2] * bT

            // Chromatic adaptation
            val rAF: Float =
                pow(frame.fl * abs(rD) / 100.0, 0.42).toFloat()
            val gAF: Float =
                pow(frame.fl * abs(gD) / 100.0, 0.42).toFloat()
            val bAF: Float =
                pow(frame.fl * abs(bD) / 100.0, 0.42).toFloat()
            val rA: Float = signum(rD.toDouble()) * 400.0f * rAF / (rAF + 27.13f)
            val gA: Float = signum(gD.toDouble()) * 400.0f * gAF / (gAF + 27.13f)
            val bA: Float = signum(bD.toDouble()) * 400.0f * bAF / (bAF + 27.13f)

            // redness-greenness
            val a = (11.0 * rA + -12.0 * gA + bA).toFloat() / 11.0f
            // yellowness-blueness
            val b = (rA + gA - 2.0 * bA).toFloat() / 9.0f

            // auxiliary components
            val u = (20.0f * rA + 20.0f * gA + 21.0f * bA) / 20.0f
            val p2 = (40.0f * rA + 20.0f * gA + bA) / 20.0f

            // hue
            val atan2: Float = atan2(b.toDouble(), a.toDouble()).toFloat()
            val atanDegrees: Float = atan2 * 180.0f / PI.toFloat()
            val hue =
                if (atanDegrees < 0) atanDegrees + 360.0f else if (atanDegrees >= 360) atanDegrees - 360.0f else atanDegrees
            val hueRadians: Float = hue * PI.toFloat() / 180.0f

            // achromatic response to color
            val ac: Float = p2 * frame.nbb

            // CAM16 lightness and brightness
            val j: Float = 100.0f * pow(
                (ac / frame.aw).toDouble(),
                (frame.c * frame.z).toDouble()
            ).toFloat()
            val q: Float = ((4.0f
                    / frame.c) * sqrt((j / 100.0f).toDouble())
                .toFloat() * (frame.aw + 4.0f)
                    * frame.flRoot)

            // CAM16 chroma, colorfulness, and saturation.
            val huePrime = if (hue < 20.14) hue + 360 else hue
            val eHue: Float =
                0.25f * (cos(huePrime * PI / 180.0 + 2.0) + 3.8).toFloat()
            val p1: Float = 50000.0f / 13.0f * eHue * frame.nc * frame.ncb
            val t: Float =
                p1 * sqrt((a * a + b * b).toDouble()).toFloat() / (u + 0.305f)
            val alpha: Float = pow(t.toDouble(), 0.9).toFloat() * pow(
                1.64 - pow(0.29, frame.n.toDouble()),
                0.73
            ).toFloat()
            // CAM16 chroma, colorfulness, saturation
            val c: Float = alpha * sqrt(j / 100.0).toFloat()
            val m: Float = c * frame.flRoot
            val s: Float =
                50.0f * sqrt((alpha * frame.c / (frame.aw + 4.0f)).toDouble())
                    .toFloat()

            // CAM16-UCS components
            val jstar = (1.0f + 100.0f * 0.007f) * j / (1.0f + 0.007f * j)
            val mstar: Float =
                1.0f / 0.0228f * ln((1.0f + 0.0228f * m).toDouble()).toFloat()
            val astar: Float = mstar * cos(hueRadians.toDouble()).toFloat()
            val bstar: Float = mstar * sin(hueRadians.toDouble()).toFloat()
            return Cam(hue, c, j, q, m, s, jstar, astar, bstar)
        }

        /**
         * Create a CAM from lightness, chroma, and hue coordinates. It is assumed those coordinates
         * were measured in the sRGB standard frame.
         */
        private fun fromJch(j: Float, c: Float, h: Float): Cam {
            return fromJchInFrame(j, c, h, Frame.DEFAULT)
        }

        /**
         * Create a CAM from lightness, chroma, and hue coordinates, and also specify the frame in which
         * the color is being viewed.
         */
        private fun fromJchInFrame(
            j: Float,
            c: Float,
            h: Float,
            frame: Frame
        ): Cam {
            val q: Float = ((4.0f
                    / frame.c) * sqrt(j / 100.0)
                .toFloat() * (frame.aw + 4.0f)
                    * frame.flRoot)
            val m: Float = c * frame.flRoot
            val alpha: Float = c / sqrt(j / 100.0).toFloat()
            val s: Float =
                50.0f * sqrt((alpha * frame.c / (frame.aw + 4.0f)).toDouble())
                    .toFloat()
            val hueRadians: Float = h * PI.toFloat() / 180.0f
            val jstar = (1.0f + 100.0f * 0.007f) * j / (1.0f + 0.007f * j)
            val mstar: Float = 1.0f / 0.0228f * ln(1.0 + 0.0228 * m).toFloat()
            val astar: Float = mstar * cos(hueRadians.toDouble()).toFloat()
            val bstar: Float = mstar * sin(hueRadians.toDouble()).toFloat()
            return Cam(h, c, j, q, m, s, jstar, astar, bstar)
        }

        /**
         * Given a hue & chroma in CAM16, L* in L*a*b*, and the frame in which the color will be
         * viewed,
         * return an ARGB integer.
         *
         *
         * The chroma of the color returned may, and frequently will, be lower than requested. This
         * is
         * a fundamental property of color that cannot be worked around by engineering. For example, a
         * red
         * hue, with high chroma, and high L* does not exist: red hues have a maximum chroma below 10
         * in
         * light shades, creating pink.
         */
        fun getInt(
            hue: Float,
            chroma: Float,
            lstar: Float,
            frame: Frame
        ): Int {
            // This is a crucial routine for building a color system, CAM16 itself is not sufficient.
            //
            // * Why these dimensions?
            // Hue and chroma from CAM16 are used because they're the most accurate measures of those
            // quantities. L* from L*a*b* is used because it correlates with luminance, luminance is
            // used to measure contrast for a11y purposes, thus providing a key constraint on what
            // colors
            // can be used.
            //
            // * Why is this routine required to build a color system?
            // In all perceptually accurate color spaces (i.e. L*a*b* and later), `chroma` may be
            // impossible for a given `hue` and `lstar`.
            // For example, a high chroma light red does not exist - chroma is limited to below 10 at
            // light red shades, we call that pink. High chroma light green does exist, but not dark
            // Also, when converting from another color space to RGB, the color may not be able to be
            // represented in RGB. In those cases, the conversion process ends with RGB values
            // outside 0-255
            // The vast majority of color libraries surveyed simply round to 0 to 255. That is not an
            // option for this library, as it distorts the expected luminance, and thus the expected
            // contrast needed for a11y
            //
            // * What does this routine do?
            // Dealing with colors in one color space not fitting inside RGB is, loosely referred to as
            // gamut mapping or tone mapping. These algorithms are traditionally idiosyncratic, there is
            // no universal answer. However, because the intent of this library is to build a system for
            // digital design, and digital design uses luminance to measure contrast/a11y, we have one
            // very important constraint that leads to an objective algorithm: the L* of the returned
            // color _must_ match the requested L*.
            //
            // Intuitively, if the color must be distorted to fit into the RGB gamut, and the L*
            // requested *must* be fulfilled, than the hue or chroma of the returned color will need
            // to be different from the requested hue/chroma.
            //
            // After exploring both options, it was more intuitive that if the requested chroma could
            // not be reached, it used the highest possible chroma. The alternative was finding the
            // closest hue where the requested chroma could be reached, but that is not nearly as
            // intuitive, as the requested hue is so fundamental to the color description.

            // If the color doesn't have meaningful chroma, return a gray with the requested Lstar.
            //
            // Yellows are very chromatic at L = 100, and blues are very chromatic at L = 0. All the
            // other hues are white at L = 100, and black at L = 0. To preserve consistency for users of
            // this system, it is better to simply return white at L* > 99, and black and L* < 0.
            var mutableHue = hue
            if (frame == Frame.DEFAULT) {
                // If the viewing conditions are the same as the default sRGB-like viewing conditions,
                // skip to using HctSolver: it uses geometrical insights to find the closest in-gamut
                // match to hue/chroma/lstar.
                return HctSolver.solveToInt(
                    mutableHue.toDouble(),
                    chroma.toDouble(),
                    lstar.toDouble()
                )
            }
            if (chroma < 1.0 || round(lstar) <= 0.0 || round(lstar) >= 100.0) {
                return CamUtils.intFromLstar(lstar)
            }
            mutableHue = if (mutableHue < 0) 0f else min(360f, mutableHue)

            // The highest chroma possible. Updated as binary search proceeds.
            var high = chroma

            // The guess for the current binary search iteration. Starts off at the highest chroma,
            // thus, if a color is possible at the requested chroma, the search can stop after one try.
            var mid = chroma
            var low = 0.0f
            var isFirstLoop = true
            var answer: Cam? = null
            while (abs(low - high) >= CHROMA_SEARCH_ENDPOINT) {
                // Given the current chroma guess, mid, and the desired hue, find J, lightness in
                // CAM16 color space, that creates a color with L* = `lstar` in the L*a*b* color space.
                val possibleAnswer = findCamByJ(mutableHue, mid, lstar)
                if (isFirstLoop) {
                    return if (possibleAnswer != null) {
                        possibleAnswer.viewed(frame)
                    } else {
                        // If this binary search iteration was the first iteration, and this point
                        // has been reached, it means the requested chroma was not available at the
                        // requested hue and L*.
                        // Proceed to a traditional binary search that starts at the midpoint between
                        // the requested chroma and 0.
                        isFirstLoop = false
                        mid = low + (high - low) / 2.0f
                        continue
                    }
                }
                if (possibleAnswer == null) {
                    // There isn't a CAM16 J that creates a color with L* `lstar`. Try a lower chroma.
                    high = mid
                } else {
                    answer = possibleAnswer
                    // It is possible to create a color. Try higher chroma.
                    low = mid
                }
                mid = low + (high - low) / 2.0f
            }

            // There was no answer: meaning, for the desired hue, there was no chroma low enough to
            // generate a color with the desired L*.
            // All values of L* are possible when there is 0 chroma. Return a color with 0 chroma, i.e.
            // a shade of gray, with the desired L*.
            return answer?.viewed(frame) ?: CamUtils.intFromLstar(
                lstar
            )
        }

        // Find J, lightness in CAM16 color space, that creates a color with L* = `lstar` in the L*a*b*
        // color space.
        //
        // Returns null if no J could be found that generated a color with L* `lstar`.
        private fun findCamByJ(hue: Float, chroma: Float, lstar: Float): Cam? {
            var low = 0.0f
            var high = 100.0f
            var mid: Float
            var bestdL = 1000.0f
            var bestdE = 1000.0f
            var bestCam: Cam? = null
            while (abs(low - high) > LIGHTNESS_SEARCH_ENDPOINT) {
                mid = low + (high - low) / 2
                // Create the intended CAM color
                val camBeforeClip = fromJch(mid, chroma, hue)
                // Convert the CAM color to RGB. If the color didn't fit in RGB, during the conversion,
                // the initial RGB values will be outside 0 to 255. The final RGB values are clipped to
                // 0 to 255, distorting the intended color.
                val clipped = camBeforeClip.viewedInSrgb()
                val clippedLstar: Float =
                    CamUtils.lstarFromInt(clipped)
                val dL: Float = abs(lstar - clippedLstar)

                // If the clipped color's L* is within error margin...
                if (dL < DL_MAX) {
                    // ...check if the CAM equivalent of the clipped color is far away from intended CAM
                    // color. For the intended color, use lightness and chroma from the clipped color,
                    // and the intended hue. Callers are wondering what the lightness is, they know
                    // chroma may be distorted, so the only concern here is if the hue slipped too far.
                    val camClipped = fromInt(clipped)
                    val dE = camClipped.distance(
                        fromJch(camClipped.j, camClipped.chroma, hue)
                    )
                    if (dE <= DE_MAX) {
                        bestdL = dL
                        bestdE = dE
                        bestCam = camClipped
                    }
                }

                // If there's no error at all, there's no need to search more.
                //
                // Note: this happens much more frequently than expected, but this is a very delicate
                // property which relies on extremely precise sRGB <=> XYZ calculations, as well as fine
                // tuning of the constants that determine error margins and when the binary search can
                // terminate.
                if (bestdL == 0f && bestdE == 0f) {
                    break
                }
                if (clippedLstar < lstar) {
                    low = mid
                } else {
                    high = mid
                }
            }
            return bestCam
        }
    }
}