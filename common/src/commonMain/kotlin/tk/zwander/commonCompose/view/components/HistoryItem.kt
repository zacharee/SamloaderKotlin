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
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.data.changelog.Changelog
import tk.zwander.common.util.invoke
import tk.zwander.samloaderkotlin.resources.MR

/**
 * An item in the firmware history list.
 * @param info the information about this item.
 * @param onDownload called when the user hits the "Download" button.
 * @param onDecrypt called when the user hits the "Decrypt" button.
 */
@Composable
internal fun HistoryItem(
    index: Int,
    info: HistoryInfo,
    changelog: Changelog?,
    changelogExpanded: Boolean,
    onChangelogExpanded: (Boolean) -> Unit,
    onDownload: (fw: String) -> Unit,
    onDecrypt: (fw: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        TransparencyCard(
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
                        text = "${index + 1}. ${stringResource(MR.strings.android, info.androidVersion 
                            ?: changelog?.androidVer?.let { Regex("[0-9]+").find(it)?.value } 
                            ?: stringResource(MR.strings.unknown))}",
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
                            painterResource(MR.images.download),
                            stringResource(MR.strings.download),
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
                            painterResource(MR.images.lock_open_outline),
                            stringResource(MR.strings.decrypt),
                            Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(MR.strings.buildDate, info.date?.format("MMM dd, yyyy") ?: stringResource(MR.strings.unknown)),
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
                        label = { Text(stringResource(MR.strings.firmware)) },
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
                                stringResource(MR.strings.changelog),
                                modifier = Modifier.fillMaxWidth(),
                            ) { onChangelogExpanded(it) }

                            AnimatedVisibility(
                                visible = changelogExpanded
                            ) {
                                Column {
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
}
