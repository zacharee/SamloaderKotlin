package tk.zwander.commonCompose.monet

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import korlibs.crypto.encoding.hex
import kotlin.math.abs
import kotlin.math.round


/**
 * Provides information about the colors of a wallpaper.
 *
 *
 * Exposes the 3 most visually representative colors of a wallpaper. Can be either
 * [WallpaperColors.primaryColor], [WallpaperColors.secondaryColor]
 * or [WallpaperColors.tertiaryColor].
 */
class WallpaperColors {
    annotation class ColorsHints

    private val mMainColors: MutableList<Color>
    private val mAllColors: MutableMap<Int, Int>

    /**
     * Returns the color hints for this instance.
     * @return The color hints.
     */
    @get:ColorsHints
    var colorHints: Int
        private set

    /**
     * Constructs a new object from three colors.
     *
     * @param primaryColor Primary color.
     * @param secondaryColor Secondary color.
     * @param tertiaryColor Tertiary color.
     */
    constructor(
        primaryColor: Color, secondaryColor: Color?,
        tertiaryColor: Color?
    ) : this(primaryColor, secondaryColor, tertiaryColor, 0) {

        // Calculate dark theme support based on primary color.
        val tmpHsl = FloatArray(3)
        ColorUtils.colorToHSL(primaryColor.toArgb(), tmpHsl)
        val luminance = tmpHsl[2]
        if (luminance < DARK_THEME_MEAN_LUMINANCE) {
            colorHints = colorHints or HINT_SUPPORTS_DARK_THEME
        }
    }

    /**
     * Constructs a new object from three colors, where hints can be specified.
     *
     * @param primaryColor Primary color.
     * @param secondaryColor Secondary color.
     * @param tertiaryColor Tertiary color.
     * @param colorHints A combination of color hints.
     */
    constructor(
        primaryColor: Color, secondaryColor: Color?,
        tertiaryColor: Color?, @ColorsHints colorHints: Int
    ) {
        mMainColors = ArrayList(3)
        mAllColors = HashMap()
        mMainColors.add(primaryColor)
        mAllColors[primaryColor.toArgb()] = 0
        if (secondaryColor != null) {
            mMainColors.add(secondaryColor)
            mAllColors[secondaryColor.toArgb()] = 0
        }
        if (tertiaryColor != null) {
            if (secondaryColor == null) {
                throw IllegalArgumentException(
                    "tertiaryColor can't be specified when "
                            + "secondaryColor is null"
                )
            }
            mMainColors.add(tertiaryColor)
            mAllColors[tertiaryColor.toArgb()] = 0
        }
        this.colorHints = colorHints
    }

    /**
     * Constructs a new object from a set of colors, where hints can be specified.
     *
     * @param colorToPopulation Map with keys of colors, and value representing the number of
     * occurrences of color in the wallpaper.
     * @param colorHints        A combination of color hints.
     * @hide
     * @see WallpaperColors.HINT_SUPPORTS_DARK_TEXT
     */
    constructor(
        colorToPopulation: MutableMap<Int, Int>,
        @ColorsHints colorHints: Int
    ) {
        mAllColors = colorToPopulation
        val colorToCam: MutableMap<Int, Cam> = HashMap()
        for (color: Int in colorToPopulation.keys) {
            colorToCam[color] = Cam.fromInt(color)
        }
        val hueProportions = hueProportions(colorToCam, colorToPopulation)
        val colorToHueProportion = colorToHueProportion(
            colorToPopulation.keys, colorToCam, hueProportions
        )
        val colorToScore: MutableMap<Int, Double> = HashMap()
        for (mapEntry: Map.Entry<Int, Double> in colorToHueProportion.entries) {
            val color = mapEntry.key
            val proportion = mapEntry.value
            val score = score(colorToCam[color], proportion)
            colorToScore[color] = score
        }
        val mapEntries: ArrayList<Map.Entry<Int, Double>> =
            ArrayList(colorToScore.entries)
        mapEntries.sortWith { a: Map.Entry<Int?, Double?>, b: Map.Entry<Int?, Double> ->
            b.value.compareTo(
                (a.value)!!
            )
        }
        val colorsByScoreDescending: MutableList<Int> = ArrayList()
        for (colorToScoreEntry: Map.Entry<Int, Double?> in mapEntries) {
            colorsByScoreDescending.add(colorToScoreEntry.key)
        }
        val mainColorInts: MutableList<Int> = ArrayList()
        findSeedColorLoop@ for (color: Int in colorsByScoreDescending) {
            val cam = colorToCam[color]
            for (otherColor: Int in mainColorInts) {
                val otherCam = colorToCam[otherColor]
                if (hueDiff(cam, otherCam) < 15) {
                    continue@findSeedColorLoop
                }
            }
            mainColorInts.add(color)
        }
        val mainColors: MutableList<Color> =
            ArrayList()
        for (colorInt: Int in mainColorInts) {
            mainColors.add(Color(colorInt))
        }
        mMainColors = mainColors
        this.colorHints = colorHints
    }

    val primaryColor: Color
        /**
         * Gets the most visually representative color of the wallpaper.
         * "Visually representative" means easily noticeable in the image,
         * probably happening at high frequency.
         *
         * @return A color.
         */
        get() = mMainColors[0]

    val secondaryColor: Color?
        /**
         * Gets the second most preeminent color of the wallpaper. Can be null.
         *
         * @return A color, may be null.
         */
        get() = if (mMainColors.size < 2) null else mMainColors[1]

    val tertiaryColor: Color?
        /**
         * Gets the third most preeminent color of the wallpaper. Can be null.
         *
         * @return A color, may be null.
         */
        get() = if (mMainColors.size < 3) null else mMainColors[2]
    val mainColors: List<Color>
        /**
         * List of most preeminent colors, sorted by importance.
         *
         * @return List of colors.
         * @hide
         */
        get() = mMainColors.toList()
    val allColors: Map<Int, Int>
        /**
         * Map of all colors. Key is rgb integer, value is importance of color.
         *
         * @return List of colors.
         * @hide
         */
        get() = mAllColors.toMap()

    override fun equals(other: Any?): Boolean {
        return other is WallpaperColors &&
                ((mMainColors == other.mMainColors) &&
                        (mAllColors == other.mAllColors) &&
                        (colorHints == other.colorHints))
    }

    override fun hashCode(): Int {
        return (31 * mMainColors.hashCode() * mAllColors.hashCode()) + colorHints
    }

    override fun toString(): String {
        val colors = StringBuilder()
        for (i in mMainColors.indices) {
            colors.append(mMainColors[i].toArgb().hex).append(" ")
        }
        return "[WallpaperColors: " + colors.toString() + "h: " + colorHints + "]"
    }

    companion object {
        private const val DEBUG_DARK_PIXELS = false

        /**
         * Specifies that dark text is preferred over the current wallpaper for best presentation.
         *
         *
         * eg. A launcher may set its text color to black if this flag is specified.
         */
        const val HINT_SUPPORTS_DARK_TEXT = 1 shl 0

        /**
         * Specifies that dark theme is preferred over the current wallpaper for best presentation.
         *
         *
         * eg. A launcher may set its drawer color to black if this flag is specified.
         */
        const val HINT_SUPPORTS_DARK_THEME = 1 shl 1

        /**
         * Specifies that this object was generated by extracting colors from a bitmap.
         * @hide
         */
        const val HINT_FROM_BITMAP = 1 shl 2

        // Maximum size that a bitmap can have to keep our calculations valid
        private const val MAX_BITMAP_SIZE = 112

        // Even though we have a maximum size, we'll mainly match bitmap sizes
        // using the area instead. This way our comparisons are aspect ratio independent.
        private const val MAX_WALLPAPER_EXTRACTION_AREA = MAX_BITMAP_SIZE * MAX_BITMAP_SIZE

        // When extracting the main colors, only consider colors
        // present in at least MIN_COLOR_OCCURRENCE of the image
        private const val MIN_COLOR_OCCURRENCE = 0.05f

        // Decides when dark theme is optimal for this wallpaper
        private const val DARK_THEME_MEAN_LUMINANCE = 0.3f

        // Minimum mean luminosity that an image needs to have to support dark text
        private const val BRIGHT_IMAGE_MEAN_LUMINANCE = 0.7f

        // We also check if the image has dark pixels in it,
        // to avoid bright images with some dark spots.
        private const val DARK_PIXEL_CONTRAST = 5.5f
        private const val MAX_DARK_AREA = 0.05f

        private fun hueDiff(a: Cam?, b: Cam?): Double {
            return (180f - abs(abs(a!!.hue - b!!.hue) - 180f)).toDouble()
        }

        private fun score(cam: Cam?, proportion: Double): Double {
            return cam!!.chroma + (proportion * 100)
        }

        private fun colorToHueProportion(
            colors: Set<Int>,
            colorToCam: Map<Int, Cam>, hueProportions: DoubleArray
        ): Map<Int, Double> {
            val colorToHueProportion: MutableMap<Int, Double> = HashMap()
            for (color: Int in colors) {
                val hue = wrapDegrees(
                    round(
                        colorToCam[color]!!.hue
                    ).toInt()
                )
                var proportion = 0.0
                for (i in hue - 15 until (hue + 15)) {
                    proportion += hueProportions[wrapDegrees(i)]
                }
                colorToHueProportion[color] = proportion
            }
            return colorToHueProportion
        }

        private fun wrapDegrees(degrees: Int): Int {
            return if (degrees < 0) {
                (degrees % 360) + 360
            } else if (degrees >= 360) {
                degrees % 360
            } else {
                degrees
            }
        }

        private fun hueProportions(
            colorToCam: Map<Int, Cam>,
            colorToPopulation: Map<Int, Int>
        ): DoubleArray {
            val proportions = DoubleArray(360)
            var totalPopulation = 0.0
            for (entry: Map.Entry<Int, Int> in colorToPopulation.entries) {
                totalPopulation += entry.value.toDouble()
            }
            for (entry: Map.Entry<Int, Int> in colorToPopulation.entries) {
                val color = entry.key
                val population = (colorToPopulation[color])!!
                val cam = colorToCam[color]
                val hue = wrapDegrees(
                    round(
                        cam!!.hue
                    ).toInt()
                )
                proportions[hue] = proportions[hue] + (population.toDouble() / totalPopulation)
            }
            return proportions
        }
    }
}