package tk.zwander.commonCompose.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import kotlin.math.min

data class Tag(
    val name: String,
    val style: SpanStyle,
) {
    fun open() = "<$name>"
    fun close() = "</$name>"
}

/**
 * The tags to interpret.
 */
private val tags = listOf(
    Tag(
        name = "b",
        style = SpanStyle(fontWeight = FontWeight.Bold),
    ),
    Tag(
        name = "i",
        style = SpanStyle(fontStyle = FontStyle.Italic),
    ),
    Tag(
        name = "u",
        style = SpanStyle(textDecoration = TextDecoration.Underline),
    ),
)

/**
 * The main entry point. Call this on a String and use the result in a Text.
 */
fun String.parseHtml(): AnnotatedString {
    val newlineReplace = this.replace("<br>", "\n")

    return buildAnnotatedString {
        recurse(newlineReplace, this)
    }
}

/**
 * Recurses through the given HTML String to convert it to an AnnotatedString.
 *
 * @param string the String to examine.
 * @param to the AnnotatedString to append to.
 */
private fun recurse(string: String, to: AnnotatedString.Builder) {
    //Find the opening tag that the given String starts with, if any.
    val startTag = tags.find { string.startsWith(it.open()) }

    //Find the closing tag that the given String starts with, if any.
    val endTag = tags.find { string.startsWith(it.close()) }

    when {
        //If the String starts with a closing tag, then pop the latest-applied
        //SpanStyle and continue recursing.
        tags.any { string.startsWith(it.close()) } -> {
            to.pop()
            recurse(string.removeRange(0, endTag!!.close().length), to)
        }
        //If the String starts with an opening tag, apply the appropriate
        //SpanStyle and continue recursing.
        tags.any { string.startsWith(it.open()) } -> {
            to.pushStyle(startTag!!.style)
            recurse(string.removeRange(0, startTag.open().length), to)
        }
        //If the String doesn't start with an opening or closing tag, but does contain either,
        //find the lowest index (that isn't -1/not found) for either an opening or closing tag.
        //Append the text normally up until that lowest index, and then recurse starting from that index.
        tags.any { string.contains(it.open()) || string.contains(it.close()) } -> {
            val firstStart = tags.map { string.indexOf(it.open()) }.filterNot { it == -1 }.minOrNull() ?: -1
            val firstEnd = tags.map { string.indexOf(it.close()) }.filterNot { it == -1 }.minOrNull() ?: -1
            val first = when {
                firstStart == -1 -> firstEnd
                firstEnd == -1 -> firstStart
                else -> min(firstStart, firstEnd)
            }

            to.append(string.substring(0, first))

            recurse(string.removeRange(0, first), to)
        }
        //There weren't any supported tags found in the text. Just append it all normally.
        else -> {
            to.append(string)
        }
    }
}
