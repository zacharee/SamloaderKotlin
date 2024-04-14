package tk.zwander.commonCompose.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import io.ktor.http.URLBuilder
import tk.zwander.common.util.LocalPhoneInfo
import tk.zwander.common.util.UrlHandler
import tk.zwander.samloaderkotlin.resources.MR

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhoneInfoView(
    modifier: Modifier = Modifier,
) {
    val phoneInfo = LocalPhoneInfo.current
    val clipboard = LocalClipboardManager.current
    var expanded by remember {
        mutableStateOf(true)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExpandButton(
            expanded = expanded,
            text = stringResource(MR.strings.phoneInfo),
            onExpandChange = { expanded = it },
            modifier = Modifier.fillMaxWidth(),
        )

        AnimatedVisibility(
            visible = !expanded,
        ) {
            SelectionContainer {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(MR.strings.tacFormat, phoneInfo?.tac ?: ""),
                    )

                    Text(
                        text = stringResource(MR.strings.modelFormat, phoneInfo?.model ?: ""),
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Button(
                            onClick = {
                                clipboard.setText(buildAnnotatedString {
                                    appendLine("TAC,Model")
                                    appendLine("${phoneInfo?.tac},${phoneInfo?.model}")
                                })
                            },
                        ) {
                            Text(text = stringResource(MR.strings.copy))
                        }

                        Button(
                            onClick = {
                                val urlBuilder =
                                    URLBuilder("https://github.com/zacharee/SamloaderKotlin/issues/new")
                                urlBuilder.parameters["template"] = "imei-database-request.md"
                                urlBuilder.parameters["title"] =
                                    "[Device IMEI Request] ${phoneInfo?.model}"
                                urlBuilder.parameters["body"] = buildString {
                                    appendLine("TAC,Model")
                                    appendLine("${phoneInfo?.tac},${phoneInfo?.model}")
                                }

                                UrlHandler.launchUrl(urlBuilder.buildString(), true)
                            },
                        ) {
                            Text(text = stringResource(MR.strings.fileIssue))
                        }
                    }
                }
            }
        }
    }
}