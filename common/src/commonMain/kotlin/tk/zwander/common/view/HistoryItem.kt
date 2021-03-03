package tk.zwander.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                OutlinedTextField(
                    value = info.date.format("MMM dd, yyyy"),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    label = { Text("Date") }
                )

                Spacer(Modifier.width(8.dp))

                OutlinedTextField(
                    value = info.androidVersion,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    label = { Text("Android Version") }
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = info.firmwareString,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Firmware") }
            )
        }
    }
}