package tk.zwander.commonCompose.view.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import korlibs.platform.Platform
import my.nanihadesuka.compose.LazyStaggeredGridVerticalScrollbarNew
import my.nanihadesuka.compose.ScrollbarSelectionMode
import tk.zwander.common.data.IOptionItem
import tk.zwander.common.util.BifrostSettings
import tk.zwander.common.util.LocalPhoneInfo
import tk.zwander.common.util.isWindows11
import tk.zwander.commonCompose.util.ThemeConstants
import tk.zwander.commonCompose.util.grid.AdaptiveFixed
import tk.zwander.commonCompose.view.LocalMenuBarHeight
import tk.zwander.commonCompose.view.components.FooterView
import tk.zwander.commonCompose.view.components.PhoneInfoView
import tk.zwander.commonCompose.view.components.settingsitems.ActionPreference
import tk.zwander.commonCompose.view.components.settingsitems.BooleanPreference
import tk.zwander.samloaderkotlin.resources.MR

val options = arrayListOf<IOptionItem>().apply {
    if (Platform.isJvm && !Platform.isAndroid) {
        add(
            IOptionItem.BasicOptionItem.BooleanItem(
                label = MR.strings.useNativeFilePicker,
                desc = MR.strings.useNativeFilePickerDesc,
                key = BifrostSettings.Keys.useNativeFileDialog,
            ),
        )
    }

    if (isWindows11) {
        add(
            IOptionItem.BasicOptionItem.BooleanItem(
                label = MR.strings.useMicaEffect,
                desc = MR.strings.useMicaEffectDesc,
                key = BifrostSettings.Keys.useMicaEffect,
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
}

@Composable
fun SettingsAboutView() {
    val gridState = rememberLazyStaggeredGridState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyStaggeredGridVerticalScrollbarNew(
            state = gridState,
            modifier = Modifier.weight(1f),
            thumbColor = ThemeConstants.Colors.scrollbarUnselected,
            thumbSelectedColor = ThemeConstants.Colors.scrollbarSelected,
            alwaysShowScrollBar = true,
            padding = ThemeConstants.Dimensions.scrollbarPadding,
            thickness = ThemeConstants.Dimensions.scrollbarThickness,
            selectionMode = ScrollbarSelectionMode.Disabled,
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

                            // TODO: Layouts for other settings types.
                        }
                    }
                }
            }
        }

        if (Platform.isAndroid && LocalPhoneInfo.current != null) {
            PhoneInfoView(modifier = Modifier.fillMaxWidth())
        }

        FooterView(
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

