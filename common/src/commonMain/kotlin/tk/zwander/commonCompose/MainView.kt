package tk.zwander.commonCompose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.zwander.kmp.platform.HostOS
import kotlinx.coroutines.launch
import tk.zwander.commonCompose.locals.ProvideModels
import tk.zwander.commonCompose.view.LocalPagerState
import tk.zwander.commonCompose.view.LocalUseTransparencyEffects
import tk.zwander.commonCompose.view.components.BifrostTheme
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

        BifrostTheme {
            val useTransparency = LocalUseTransparencyEffects.current

            Surface(
                color = if (useTransparency) Color.Transparent else MaterialTheme.colorScheme.surface,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides if (useTransparency) MaterialTheme.colorScheme.onBackground else LocalContentColor.current,
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
                                userScrollEnabled = HostOS.current == HostOS.Android,
                                beyondViewportPageCount = pagerState.pageCount,
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
