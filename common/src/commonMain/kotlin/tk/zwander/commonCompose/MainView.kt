@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package tk.zwander.commonCompose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import korlibs.io.async.launch
import korlibs.memory.Platform
import tk.zwander.commonCompose.locals.LocalDecryptModel
import tk.zwander.commonCompose.locals.LocalDownloadModel
import tk.zwander.commonCompose.locals.LocalHistoryModel
import tk.zwander.commonCompose.locals.ProvideModels
import tk.zwander.commonCompose.view.LocalPagerState
import tk.zwander.commonCompose.view.LocalUseMicaEffect
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
        val pagerState = LocalPagerState.current

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
            val useMicaEffect = LocalUseMicaEffect.current

            Surface(
                color = if (useMicaEffect) Color.Transparent else MaterialTheme.colorScheme.surface,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides if (useMicaEffect) MaterialTheme.colorScheme.onBackground else LocalContentColor.current,
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
                                beyondBoundsPageCount = pagerState.pageCount,
                            ) {
                                pages[it].render()
                            }
                        }

                        TabView(
                            selectedPage = pagerState.currentPage,
                            onPageSelected = {
                                scope.launch {
                                    pagerState.animateScrollToPage(it)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
