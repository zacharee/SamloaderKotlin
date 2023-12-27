package tk.zwander.commonCompose.view.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ktor.http.URLBuilder
import korlibs.memory.Platform
import tk.zwander.common.util.BifrostSettings
import tk.zwander.common.util.SettingsKey
import tk.zwander.common.util.invoke
import tk.zwander.common.util.rememberPhoneInfo
import tk.zwander.commonCompose.view.components.ExpandButton
import tk.zwander.commonCompose.view.components.FooterView
import tk.zwander.samloaderkotlin.resources.MR
import tk.zwander.common.util.UrlHandler

data class OptionItem<T>(
    val label: String,
    val desc: String?,
    val key: SettingsKey<T>,
)

val options = arrayListOf<OptionItem<*>>().apply {
    if (Platform.isJvm && !Platform.isAndroid) {
        add(OptionItem(
            label = MR.strings.useNativeFilePicker(),
            desc = MR.strings.useNativeFilePickerDesc(),
            key = BifrostSettings.Keys.useNativeFileDialog,
        ))
    }

    add(OptionItem(
        label = MR.strings.allowLowercaseCharacters(),
        desc = MR.strings.allowLowercaseCharactersDesc(),
        key = BifrostSettings.Keys.allowLowercaseCharacters,
    ))
    add(OptionItem(
        label = MR.strings.autoDeleteEncryptedFirmware(),
        desc = MR.strings.autoDeleteEncryptedFirmwareDesc(),
        key = BifrostSettings.Keys.autoDeleteEncryptedFirmware,
    ))
}

@Composable
fun SettingsAboutView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            modifier = Modifier.weight(1f),
        ) {
            items(items = options, key = { it.key.key }) { item ->
                Box(
                    modifier = Modifier.widthIn(max = 400.dp),
                ) {
                    when (item.key) {
                        is SettingsKey.Boolean -> {
                            BooleanPreference(item = item as OptionItem<Boolean>)
                        }
                        // TODO: Layouts for other settings types.
                        else -> {}
                    }
                }
            }
        }

        if (Platform.isAndroid) {
            PhoneInfoView(modifier = Modifier.fillMaxWidth())
        }

        FooterView(
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhoneInfoView(
    modifier: Modifier = Modifier,
) {
    val phoneInfo = rememberPhoneInfo()
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
            text = MR.strings.phoneInfo(),
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
                        text = MR.strings.tacFormat(phoneInfo?.tac ?: ""),
                    )

                    Text(
                        text = MR.strings.modelFormat(phoneInfo?.model ?: ""),
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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
                            Text(text = MR.strings.copy())
                        }

                        Button(
                            onClick = {
                                val urlBuilder = URLBuilder("https://github.com/zacharee/SamloaderKotlin/issues/new")
                                urlBuilder.parameters["template"] = "imei-database-request.md"
                                urlBuilder.parameters["title"] = "[Device IMEI Request] ${phoneInfo?.model}"
                                urlBuilder.parameters["body"] = buildString {
                                    appendLine("TAC,Model")
                                    appendLine("${phoneInfo?.tac},${phoneInfo?.model}")
                                }

                                UrlHandler.launchUrl(urlBuilder.buildString(), true)
                            },
                        ) {
                            Text(text = MR.strings.fileIssue())
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BooleanPreference(
    item: OptionItem<Boolean>,
    modifier: Modifier = Modifier,
) {
    var state by item.key.collectAsMutableState()

    Card(
        modifier = modifier,
        onClick = {
            state = !(state ?: false)
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f)
                    .padding(end = 8.dp),
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )

                item.desc?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Switch(
                checked = state ?: false,
                onCheckedChange = {
                    state = it
                },
            )
        }
    }
}
