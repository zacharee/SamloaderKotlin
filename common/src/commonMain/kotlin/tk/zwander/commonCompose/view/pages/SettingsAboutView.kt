package tk.zwander.commonCompose.view.pages

import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import dev.zwander.kmp.platform.HostOS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyVerticalStaggeredGridScrollbar
import tk.zwander.common.data.IOptionItem
import tk.zwander.common.util.BifrostSettings
import tk.zwander.common.util.LocalPhoneInfo
import tk.zwander.common.util.UpdateUtil
import tk.zwander.commonCompose.util.ThemeConstants
import tk.zwander.commonCompose.util.grid.AdaptiveFixed
import tk.zwander.commonCompose.view.LocalMenuBarHeight
import tk.zwander.commonCompose.view.components.FooterView
import tk.zwander.commonCompose.view.components.PhoneInfoView
import tk.zwander.commonCompose.view.components.settingsitems.ActionPreference
import tk.zwander.commonCompose.view.components.settingsitems.BooleanPreference
import tk.zwander.commonCompose.view.components.settingsitems.LiteralPreference
import tk.zwander.samloaderkotlin.resources.MR

val options = arrayListOf<IOptionItem>().apply {
//    if (isWindows11) {
//        add(
//            IOptionItem.BasicOptionItem.BooleanItem(
//                label = MR.strings.useMicaEffect,
//                desc = MR.strings.useMicaEffectDesc,
//                key = BifrostSettings.Keys.useMicaEffect,
//            ),
//        )
//    }

    if (HostOS.current == HostOS.MacOS) {
        add(
            IOptionItem.BasicOptionItem.BooleanItem(
                label = MR.strings.useVibrancyEffect,
                desc = MR.strings.useVibrancyEffectDesc,
                key = BifrostSettings.Keys.useVibrancyEffect,
            ),
        )
    }

    add(
        IOptionItem.BasicOptionItem.BooleanItem(
            label = MR.strings.allowLowercaseCharacters,
            desc = MR.strings.allowLowercaseCharactersDesc,
            key = BifrostSettings.Keys.allowLowercaseCharacters,
        ),
    )
    add(
        IOptionItem.BasicOptionItem.BooleanItem(
            label = MR.strings.autoDeleteEncryptedFirmware,
            desc = MR.strings.autoDeleteEncryptedFirmwareDesc,
            key = BifrostSettings.Keys.autoDeleteEncryptedFirmware,
        ),
    )
    add(
        IOptionItem.BasicOptionItem.BooleanItem(
            label = MR.strings.enable_offline_decryption,
            desc = MR.strings.enable_offline_decryption_desc,
            key = BifrostSettings.Keys.enableDecryptKeySave,
        ),
    )
    add(
        IOptionItem.ActionOptionItem(
            label = MR.strings.removeSavedData,
            desc = MR.strings.removeSavedDataDesc,
            listKey = "remove_saved_data",
            action = {
                BifrostSettings.settings.clear()
            },
        ),
    )
    add(
        IOptionItem.LiteralOptionItem(
            label = MR.strings.updates,
            desc = null,
            listKey = "update_checker",
            render = {
                val scope = rememberCoroutineScope()
                var availableVersion by rememberSaveable {
                    mutableStateOf<String?>(null)
                }
                var loading by remember {
                    mutableStateOf(false)
                }

                Row(
                    modifier = it,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                            .heightIn(min = 48.dp)
                            .align(Alignment.CenterVertically),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = loading,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.Center),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                            )
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = !loading && availableVersion != null,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Text(
                                text = availableVersion
                                    ?.takeIf { version -> version.isNotBlank() }
                                    ?.let { version -> stringResource(MR.strings.update_available, version) }
                                    ?: stringResource(MR.strings.no_updates_available),
                            )
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = !loading && availableVersion == null,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Text(
                                text = stringResource(MR.strings.check_for_updates),
                            )
                        }
                    }

                    Crossfade(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        targetState = availableVersion?.isNotBlank() == true,
                    ) { updateAvailable ->
                        Box(
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            if (updateAvailable) {
                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            loading = true
                                            UpdateUtil.installUpdate()
                                            loading = false
                                        }
                                    },
                                    enabled = !loading,
                                ) {
                                    Text(text = stringResource(MR.strings.update))
                                }
                            } else {
                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            loading = true
                                            availableVersion = UpdateUtil.checkForUpdate()?.newVersion ?: ""
                                            loading = false
                                        }
                                    },
                                    enabled = !loading,
                                ) {
                                    Text(text = stringResource(MR.strings.check))
                                }
                            }
                        }
                    }
                }
            },
        ),
    )
}

@Composable
fun SettingsAboutView() {
    val gridState = rememberLazyStaggeredGridState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyVerticalStaggeredGridScrollbar(
            state = gridState,
            modifier = Modifier.weight(1f),
            settings = ThemeConstants.ScrollBarSettings.Default,
        ) {
            LazyVerticalStaggeredGrid(
                columns = AdaptiveFixed(minSize = 300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                state = gridState,
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 8.dp + LocalMenuBarHeight.current,
                    bottom = 8.dp,
                ),
            ) {
                items(items = options, key = { it.listKey }) { item ->
                    Box(
                        modifier = Modifier.widthIn(max = 400.dp),
                    ) {
                        when (item) {
                            is IOptionItem.ActionOptionItem -> {
                                ActionPreference(item = item)
                            }

                            is IOptionItem.BasicOptionItem.BooleanItem -> {
                                BooleanPreference(item = item)
                            }

                            is IOptionItem.LiteralOptionItem -> {
                                LiteralPreference(item = item)
                            }

                            // TODO: Layouts for other settings types.
                        }
                    }
                }
            }
        }

        if (HostOS.current == HostOS.Android && LocalPhoneInfo.current != null) {
            PhoneInfoView(modifier = Modifier.fillMaxWidth())
        }

        FooterView(
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

