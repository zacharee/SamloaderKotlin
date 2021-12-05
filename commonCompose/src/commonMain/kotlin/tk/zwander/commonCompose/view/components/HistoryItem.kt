package tk.zwander.commonCompose.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.data.changelog.Changelog
import tk.zwander.commonCompose.util.vectorResource

/**
 * An item in the firmware history list.
 * @param info the information about this item.
 * @param onDownload called when the user hits the "Download" button.
 * @param onDecrypt called when the user hits the "Decrypt" button.
 */
@Composable
fun HistoryItem(
    index: Int,
    info: HistoryInfo,
    changelog: Changelog?,
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
                        text = "${index + 1}. Android ${info.androidVersion}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        fontSize = 20.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.weight(1f))

//                    IconButton(
//                        onClick = {
//                            onDownload(info.firmwareString)
//                        },
//                        modifier = Modifier.align(Alignment.Bottom)
//                            .size(32.dp)
//                    ) {
//                        Icon(vectorResource("download.xml"), "Download")
//                    }
//
//                    Spacer(Modifier.width(8.dp))

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
                    Text(
                        text = "Build Date: ${info.date.format("MMM dd, yyyy")}",
                        modifier = Modifier.align(Alignment.Bottom),
                        fontSize = 16.sp,
                        lineHeight = 16.sp
                    )
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

                if (changelog != null) {
                    var changelogExpanded by remember { mutableStateOf(false) }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(Modifier.height(8.dp))

                        Column {
                            ExpandButton(
                                changelogExpanded,
                                "Changelog"
                            ) { changelogExpanded = it }

                            AnimatedVisibility(
                                visible = changelogExpanded
                            ) {
                                Spacer(Modifier.height(8.dp))

                                ChangelogDisplay(changelog)
                            }
                        }
                    }
                }
            }
        }
    }
}
