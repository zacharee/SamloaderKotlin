@file:OptIn(ExperimentalFoundationApi::class)

package tk.zwander.commonCompose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import korlibs.io.async.launch
import korlibs.memory.Platform
import tk.zwander.common.util.isWindows11
import tk.zwander.commonCompose.locals.LocalDecryptModel
import tk.zwander.commonCompose.locals.LocalDownloadModel
import tk.zwander.commonCompose.locals.LocalHistoryModel
import tk.zwander.commonCompose.locals.LocalMainModel
import tk.zwander.commonCompose.locals.ProvideModels
import tk.zwander.commonCompose.util.collectAsImmediateMutableState
import tk.zwander.commonCompose.util.pager.pagerTabIndicatorOffset
import tk.zwander.commonCompose.view.components.CustomMaterialTheme
import tk.zwander.commonCompose.view.components.TabView
import tk.zwander.commonCompose.view.components.pages
import kotlin.time.ExperimentalTime

/**
 * The main UI view.
 */
@ExperimentalTime
@Composable
fun MainView(
    modifier: Modifier = Modifier,
    fullPadding: PaddingValues = PaddingValues(),
) {
    val scope = rememberCoroutineScope()

    ProvideModels {
        var currentPage by LocalMainModel.current.currentPage.collectAsImmediateMutableState()
        val pagerState = rememberPagerState { pages.size }

        LaunchedEffect(key1 = currentPage.index) {
            pagerState.animateScrollToPage(currentPage.index)
        }
        LaunchedEffect(key1 = pagerState.currentPage, key2 = pagerState.currentPageOffsetFraction) {
            if (pagerState.currentPage == pagerState.targetPage && pagerState.currentPageOffsetFraction == 0f) {
                currentPage = pages[pagerState.currentPage]
            }
        }

        val downloadModel = LocalDownloadModel.current
        val decryptModel = LocalDecryptModel.current
        val historyModel = LocalHistoryModel.current

        scope.launch {
            downloadModel.onCreate()
        }

        scope.launch {
            decryptModel.onCreate()
        }

        scope.launch {
            historyModel.onCreate()
        }

        CustomMaterialTheme {
            Surface(
                color = if (isWindows11) Color.Transparent else MaterialTheme.colorScheme.surface,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides if (isWindows11) MaterialTheme.colorScheme.onBackground else LocalContentColor.current,
                ) {
                    Column(
                        modifier = modifier.fillMaxSize()
                            .padding(fullPadding),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = 1200.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                pageSpacing = 8.dp,
                                userScrollEnabled = Platform.isAndroid,
                            ) {
                                pages[it].render()
                            }
                        }

                        TabView(
                            selectedPage = currentPage,
                            onPageSelected = {
                                currentPage = it
                            },
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
