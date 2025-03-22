package tk.zwander.commonCompose.view

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp
import tk.zwander.commonCompose.util.pager.BifrostPagerState

val LocalMenuBarHeight = compositionLocalOf { 0.dp }
val LocalUseTransparencyEffects = compositionLocalOf { false }
val LocalPagerState = compositionLocalOf { BifrostPagerState() }
