package tk.zwander.commonCompose.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.data.changelog.Changelog
import tk.zwander.commonCompose.util.vectorResource
import tk.zwander.samloaderkotlin.resources.MR
import tk.zwander.samloaderkotlin.strings

/**
 * An item in the firmware history list.
 * @param info the information about this item.
 * @param onDownload called when the user hits the "Download" button.
 * @param onDecrypt called when the user hits the "Decrypt" button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryItem(
    index: Int,
    info: HistoryInfo,
    changelog: Changelog?,
    changelogExpanded: Boolean,
    onChangelogExpanded: (Boolean) -> Unit,
    onDownload: (fw: String) -> Unit,
    onDecrypt: (fw: String) -> Unit
) {
    Box {
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
                        text = "${index + 1}. ${strings.android(info.androidVersion 
                            ?: changelog?.androidVer?.let { Regex("[0-9]+").find(it)?.value } 
                            ?: strings.unknown())}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        fontSize = 20.sp,
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
                        Icon(
                            vectorResource(MR.assets.download),
                            strings.download(),
                            Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            onDecrypt(info.firmwareString)
                        },
                        modifier = Modifier.align(Alignment.Bottom)
                            .size(32.dp)
                    ) {
                        Icon(
                            vectorResource(MR.assets.lock_open_outline),
                            strings.decrypt(),
                            Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.buildDate(info.date?.format("MMM dd, yyyy") ?: strings.unknown()),
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
                        label = { Text(strings.firmware()) },
                        singleLine = true
                    )
                }

                if (changelog != null) {
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(Modifier.height(8.dp))

                        Column {
                            ExpandButton(
                                changelogExpanded,
                                strings.changelog()
                            ) { onChangelogExpanded(it) }

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
