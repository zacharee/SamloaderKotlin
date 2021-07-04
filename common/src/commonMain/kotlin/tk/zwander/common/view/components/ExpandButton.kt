package tk.zwander.common.view.components

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp

@Composable
fun ExpandButton(
    expanded: Boolean,
    text: String,
    onExpandChange: (Boolean) -> Unit
) {
    val expandText = buildAnnotatedString {
        append(text)
        append(" ")
        appendInlineContent("expandIcon", "[icon]")
    }
    val inlineContent = mapOf(
        "expandIcon" to InlineTextContent(
            placeholder = Placeholder(
                16.sp,
                16.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Icon(if (expanded) Icons.Filled.KeyboardArrowUp else
                Icons.Filled.KeyboardArrowDown, "")
        }
    )

    TextButton(
        onClick = {
            onExpandChange(!expanded)
        }
    ) {
        Text(
            text = expandText,
            inlineContent = inlineContent,
        )
    }
}