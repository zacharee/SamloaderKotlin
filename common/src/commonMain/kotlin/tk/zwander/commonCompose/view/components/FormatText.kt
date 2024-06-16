package tk.zwander.commonCompose.view.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp

@Composable
internal fun FormatText(
    text: String,
    textFormat: Any?,
    modifier: Modifier = Modifier,
    valueFontFamily: FontFamily? = null,
    labelFontFamily: FontFamily? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = textFormat.toString(),
            lineHeight = LocalTextStyle.current.fontSize,
            fontFamily = valueFontFamily,
            modifier = Modifier.animateContentSize().wrapContentWidth(),
        )

        ProvideTextStyle(
            value = TextStyle(
                baselineShift = BaselineShift.Superscript,
            ),
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontFamily = labelFontFamily,
            )
        }
    }
}
