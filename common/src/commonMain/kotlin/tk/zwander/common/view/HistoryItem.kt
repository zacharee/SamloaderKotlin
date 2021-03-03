package tk.zwander.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tk.zwander.common.data.HistoryInfo

@Composable
fun HistoryItem(
    info: HistoryInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = info.date.format("MMM dd, yyyy"),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    fontSize = 20.sp,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Android ${info.androidVersion}",
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Bottom)
                        .offset(y = (-1).dp),
                    color = MaterialTheme.typography.body1.color,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = info.firmwareString,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Firmware") },
                singleLine = true
            )
        }
    }
}