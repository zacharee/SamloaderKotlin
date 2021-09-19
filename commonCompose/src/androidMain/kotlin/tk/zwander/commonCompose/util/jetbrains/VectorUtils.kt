package tk.zwander.commonCompose.util.jetbrains

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.util.*

/**
 * A hacky file thrown together from JetBrains and Google components to parse
 * vector XMLs outside of the context of Android's resources.
 */

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
private const val AAPT_NS = "http://schemas.android.com/aapt"

private class BuildContext {
    val currentGroups = LinkedList<Group>()

    enum class Group {
        /**
         * Group that exists in xml file
         */
        Real,

        /**
         * Group that doesn't exist in xml file. We add it manually when we see <clip-path> node.
         * It will be automatically popped when the real group will be popped.
         */
        Virtual
    }
}

internal fun Element.parseVectorRoot(density: Density): ImageVector {
    val context = BuildContext()
    val builder = ImageVector.Builder(
        defaultWidth = attributeOrNull(ANDROID_NS, "width").parseDp(density),
        defaultHeight = attributeOrNull(ANDROID_NS, "height").parseDp(density),
        viewportWidth = attributeOrNull(ANDROID_NS, "viewportWidth")?.toFloat() ?: 0f,
        viewportHeight = attributeOrNull(ANDROID_NS, "viewportHeight")?.toFloat() ?: 0f
    )
    parseVectorNodes(builder, context)
    return builder.build()
}

private fun Element.parseVectorNodes(builder: ImageVector.Builder, context: BuildContext) {
    childrenSequence
        .filterIsInstance<Element>()
        .forEach {
            it.parseVectorNode(builder, context)
        }
}

private fun Element.parseVectorNode(builder: ImageVector.Builder, context: BuildContext) {
    when (nodeName) {
        "path" -> parsePath(builder)
        "clip-path" -> parseClipPath(builder, context)
        "group" -> parseGroup(builder, context)
    }
}

private fun Element.parsePath(builder: ImageVector.Builder) {
    builder.addPath(
        pathData = addPathNodes(attributeOrNull(ANDROID_NS, "pathData")),
        pathFillType = attributeOrNull(ANDROID_NS, "fillType")
            ?.let(::parseFillType) ?: PathFillType.NonZero,
        name = attributeOrNull(ANDROID_NS, "name") ?: "",
        fill = attributeOrNull(ANDROID_NS, "fillColor")?.let(::parseStringBrush)
            ?: apptAttr(ANDROID_NS, "fillColor")?.let(Element::parseElementBrush),
        fillAlpha = attributeOrNull(ANDROID_NS, "fillAlpha")?.toFloat() ?: 1.0f,
        stroke = attributeOrNull(ANDROID_NS, "strokeColor")?.let(::parseStringBrush)
            ?: apptAttr(ANDROID_NS, "strokeColor")?.let(Element::parseElementBrush),
        strokeAlpha = attributeOrNull(ANDROID_NS, "strokeAlpha")?.toFloat() ?: 1.0f,
        strokeLineWidth = attributeOrNull(ANDROID_NS, "strokeWidth")?.toFloat() ?: 1.0f,
        strokeLineCap = attributeOrNull(ANDROID_NS, "strokeLineCap")
            ?.let(::parseStrokeCap) ?: StrokeCap.Butt,
        strokeLineJoin = attributeOrNull(ANDROID_NS, "strokeLineJoin")
            ?.let(::parseStrokeJoin) ?: StrokeJoin.Miter,
        strokeLineMiter = attributeOrNull(ANDROID_NS, "strokeMiterLimit")?.toFloat() ?: 1.0f,
        trimPathStart = attributeOrNull(ANDROID_NS, "trimPathStart")?.toFloat() ?: 0.0f,
        trimPathEnd = attributeOrNull(ANDROID_NS, "trimPathEnd")?.toFloat() ?: 1.0f,
        trimPathOffset = attributeOrNull(ANDROID_NS, "trimPathOffset")?.toFloat() ?: 0.0f
    )
}

private fun Element.parseClipPath(builder: ImageVector.Builder, context: BuildContext) {
    builder.addGroup(
        name = attributeOrNull(ANDROID_NS, "name") ?: "",
        clipPathData = addPathNodes(attributeOrNull(ANDROID_NS, "pathData"))
    )
    context.currentGroups.addLast(BuildContext.Group.Virtual)
}

private fun Element.parseGroup(builder: ImageVector.Builder, context: BuildContext) {
    builder.addGroup(
        attributeOrNull(ANDROID_NS, "name") ?: "",
        attributeOrNull(ANDROID_NS, "rotation")?.toFloat() ?: DefaultRotation,
        attributeOrNull(ANDROID_NS, "pivotX")?.toFloat() ?: DefaultPivotX,
        attributeOrNull(ANDROID_NS, "pivotY")?.toFloat() ?: DefaultPivotY,
        attributeOrNull(ANDROID_NS, "scaleX")?.toFloat() ?: DefaultScaleX,
        attributeOrNull(ANDROID_NS, "scaleY")?.toFloat() ?: DefaultScaleY,
        attributeOrNull(ANDROID_NS, "translateX")?.toFloat() ?: DefaultTranslationX,
        attributeOrNull(ANDROID_NS, "translateY")?.toFloat() ?: DefaultTranslationY,
        EmptyPath
    )
    context.currentGroups.addLast(BuildContext.Group.Real)

    parseVectorNodes(builder, context)

    do {
        val removedGroup = context.currentGroups.removeLastOrNull()
        builder.clearGroup()
    } while (removedGroup == BuildContext.Group.Virtual)
}

private fun parseStringBrush(str: String) = SolidColor(Color(parseColorValue(str)))

private fun Element.parseElementBrush(): Brush? =
    childrenSequence
        .filterIsInstance<Element>()
        .find { it.nodeName == "gradient" }
        ?.parseGradient()

private fun Element.parseGradient(): Brush? {
    return when (attributeOrNull(ANDROID_NS, "type")) {
        "linear" -> parseLinearGradient()
        "radial" -> parseRadialGradient()
        "sweep" -> parseSweepGradient()
        else -> null
    }
}

@Suppress("CHANGING_ARGUMENTS_EXECUTION_ORDER_FOR_NAMED_VARARGS")
private fun Element.parseLinearGradient() = Brush.linearGradient(
    colorStops = parseColorStops(),
    start = Offset(
        attributeOrNull(ANDROID_NS, "startX")?.toFloat() ?: 0f,
        attributeOrNull(ANDROID_NS, "startY")?.toFloat() ?: 0f
    ),
    end = Offset(
        attributeOrNull(ANDROID_NS, "endX")?.toFloat() ?: 0f,
        attributeOrNull(ANDROID_NS, "endY")?.toFloat() ?: 0f
    ),
    tileMode = attributeOrNull(ANDROID_NS, "tileMode")?.let(::parseTileMode) ?: TileMode.Clamp
)

@Suppress("CHANGING_ARGUMENTS_EXECUTION_ORDER_FOR_NAMED_VARARGS")
private fun Element.parseRadialGradient() = Brush.radialGradient(
    colorStops = parseColorStops(),
    center = Offset(
        attributeOrNull(ANDROID_NS, "centerX")?.toFloat() ?: 0f,
        attributeOrNull(ANDROID_NS, "centerY")?.toFloat() ?: 0f
    ),
    radius = attributeOrNull(ANDROID_NS, "gradientRadius")?.toFloat() ?: 0f,
    tileMode = attributeOrNull(ANDROID_NS, "tileMode")?.let(::parseTileMode) ?: TileMode.Clamp
)

@Suppress("CHANGING_ARGUMENTS_EXECUTION_ORDER_FOR_NAMED_VARARGS")
private fun Element.parseSweepGradient() = Brush.sweepGradient(
    colorStops = parseColorStops(),
    center = Offset(
        attributeOrNull(ANDROID_NS, "centerX")?.toFloat() ?: 0f,
        attributeOrNull(ANDROID_NS, "centerY")?.toFloat() ?: 0f,
    )
)

private fun Element.parseColorStops(): Array<Pair<Float, Color>> {
    val items = childrenSequence
        .filterIsInstance<Element>()
        .filter { it.nodeName == "item" }
        .toList()

    val colorStops = items.mapIndexedNotNullTo(mutableListOf()) { index, item ->
        item.parseColorStop(defaultOffset = index.toFloat() / items.lastIndex.coerceAtLeast(1))
    }

    if (colorStops.isEmpty()) {
        val startColor = attributeOrNull(ANDROID_NS, "startColor")?.let(::parseColorValue)
        val centerColor = attributeOrNull(ANDROID_NS, "centerColor")?.let(::parseColorValue)
        val endColor = attributeOrNull(ANDROID_NS, "endColor")?.let(::parseColorValue)

        if (startColor != null) {
            colorStops.add(0f to Color(startColor))
        }
        if (centerColor != null) {
            colorStops.add(0.5f to Color(centerColor))
        }
        if (endColor != null) {
            colorStops.add(1f to Color(endColor))
        }
    }

    return colorStops.toTypedArray()
}

private fun Element.parseColorStop(defaultOffset: Float): Pair<Float, Color>? {
    val offset = attributeOrNull(ANDROID_NS, "offset")?.toFloat() ?: defaultOffset
    val color = attributeOrNull(ANDROID_NS, "color")?.let(::parseColorValue) ?: return null
    return offset to Color(color)
}

private fun Element.attributeOrNull(namespace: String, name: String): String? {
    val value = getAttributeNS(namespace, name)
    return if (value.isNotBlank()) value else null
}

/**
 * Attribute of an element can be represented as a separate child:
 *
 *  <path ...>
 *    <aapt:attr name="android:fillColor">
 *      <gradient ...
 *        ...
 *      </gradient>
 *    </aapt:attr>
 *  </path>
 *
 * instead of:
 *
 *  <path android:fillColor="red" ... />
 */
private fun Element.apptAttr(
    namespace: String,
    name: String
): Element? {
    val prefix = lookupPrefix(namespace) ?: return null
    return childrenSequence
        .filterIsInstance<Element>()
        .find {
            it.namespaceURI == AAPT_NS && it.localName == "attr" &&
                    it.getAttribute("name") == "$prefix:$name"
        }
}

private val Element.childrenSequence get() = sequence<Node> {
    for (i in 0 until childNodes.length) {
        yield(childNodes.item(i))
    }
}

private const val ALPHA_MASK = 0xFF000000

// parseColorValue is copied from Android:
// https://cs.android.com/android-studio/platform/tools/base/+/05fadd8cb2aaafb77da02048c7a240b2147ff293:sdk-common/src/main/java/com/android/ide/common/vectordrawable/VdUtil.kt;l=58
/**
 * Parses a color value in #AARRGGBB format.
 *
 * @param color the color value string
 * @return the integer color value
 */
internal fun parseColorValue(color: String): Int {
    require(color.startsWith("#")) { "Invalid color value $color" }

    return when (color.length) {
        7 -> {
            // #RRGGBB
            java.lang.Long.parseLong(color.substring(1), 16) or ALPHA_MASK
        }
        9 -> {
            // #AARRGGBB
            java.lang.Long.parseLong(color.substring(1), 16)
        }
        4 -> {
            // #RGB
            val v = java.lang.Long.parseLong(color.substring(1), 16)
            var k = (v shr 8 and 0xF) * 0x110000
            k = k or (v shr 4 and 0xF) * 0x1100
            k = k or (v and 0xF) * 0x11
            k or ALPHA_MASK
        }
        5 -> {
            // #ARGB
            val v = java.lang.Long.parseLong(color.substring(1), 16)
            var k = (v shr 12 and 0xF) * 0x11000000
            k = k or (v shr 8 and 0xF) * 0x110000
            k = k or (v shr 4 and 0xF) * 0x1100
            k = k or (v and 0xF) * 0x11
            k or ALPHA_MASK
        }
        else -> ALPHA_MASK
    }.toInt()
}

internal fun parseFillType(fillType: String): PathFillType = when (fillType) {
    "nonZero" -> PathFillType.NonZero
    "evenOdd" -> PathFillType.EvenOdd
    else -> throw UnsupportedOperationException("unknown fillType: $fillType")
}

internal fun parseStrokeCap(strokeCap: String): StrokeCap = when (strokeCap) {
    "butt" -> StrokeCap.Butt
    "round" -> StrokeCap.Round
    "square" -> StrokeCap.Square
    else -> throw UnsupportedOperationException("unknown strokeCap: $strokeCap")
}

internal fun parseStrokeJoin(strokeJoin: String): StrokeJoin = when (strokeJoin) {
    "miter" -> StrokeJoin.Miter
    "round" -> StrokeJoin.Round
    "bevel" -> StrokeJoin.Bevel
    else -> throw UnsupportedOperationException("unknown strokeJoin: $strokeJoin")
}

internal fun parseTileMode(tileMode: String): TileMode = when (tileMode) {
    "clamp" -> TileMode.Clamp
    "repeated" -> TileMode.Repeated
    "mirror" -> TileMode.Mirror
    else -> throw throw UnsupportedOperationException("unknown tileMode: $tileMode")
}

internal fun String?.parseDp(density: Density): Dp = with(density) {
    return when {
        this@parseDp == null -> 0f.dp
        endsWith("dp") -> removeSuffix("dp").toFloat().dp
        endsWith("px") -> removeSuffix("px").toFloat().toDp()
        else -> throw UnsupportedOperationException("value should ends with dp or px")
    }
}