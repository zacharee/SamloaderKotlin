package tk.zwander.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.util.vectorResource

@Composable
fun HistoryItem(
    info: HistoryInfo,
    onDownload: (fw: String) -> Unit,
    onDecrypt: (fw: String) -> Unit
) {
    Box(
        modifier = Modifier.padding(4.dp)
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

                    Spacer(Modifier.weight(1f))

                    IconButton(
                        onClick = {
                            onDownload(info.firmwareString)
                        },
                        modifier = Modifier.align(Alignment.Bottom)
                            .size(32.dp)
                    ) {
                        Icon(vectorResource("download.xml"), "Download")
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            onDecrypt(info.firmwareString)
                        },
                        modifier = Modifier.align(Alignment.Bottom)
                            .size(32.dp)
                    ) {
                        Icon(vectorResource("decrypt.xml"), "Decrypt")
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = info.firmwareString,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.weight(1f)
                            .align(Alignment.CenterVertically),
                        label = { Text("Firmware") },
                        singleLine = true
                    )
                }
            }
        }
    }
}