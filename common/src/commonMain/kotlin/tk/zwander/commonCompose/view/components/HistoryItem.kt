package tk.zwander.commonCompose.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import tk.zwander.common.data.HistoryInfo
import tk.zwander.common.data.changelog.Changelog
import tk.zwander.commonCompose.util.OffsetCorrectedIdentityTransformation
import tk.zwander.commonCompose.util.monthNames
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
    Box(modifier = modifier) {
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

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(
                            MR.strings.buildDate,
                            info.date?.let {
                                val monthNames = monthNames()
                                LocalDate.Format {
                                    monthName(monthNames)
                                    char(' ')
                                    dayOfMonth(Padding.NONE)
                                    char(',')
                                    char(' ')
                                    year()
                                }.format(it)
                            } ?: stringResource(MR.strings.unknown),
                        ),
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
                        singleLine = true,
                        visualTransformation = OffsetCorrectedIdentityTransformation(info.firmwareString),
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

                                    ChangelogDisplay(
                                        changelog = changelog,
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
