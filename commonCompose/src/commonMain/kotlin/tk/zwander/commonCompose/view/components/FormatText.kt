package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp

@Composable
fun FormatText(
    text: String,
    textFormat: Any?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = textFormat.toString(),
        )

        ProvideTextStyle(
            value = TextStyle(
                baselineShift = BaselineShift.Superscript
            )
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
            )
        }
    }
}
