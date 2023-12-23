@file:OptIn(DangerousInternalIoApi::class, ExperimentalTime::class)

package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import io.ktor.utils.io.core.internal.DangerousInternalIoApi
import korlibs.io.annotations.Keep
import tk.zwander.common.util.invoke
import tk.zwander.commonCompose.view.pages.DecryptView
import tk.zwander.commonCompose.view.pages.DownloadView
import tk.zwander.commonCompose.view.pages.HistoryView
import tk.zwander.commonCompose.view.pages.SettingsAboutView
import tk.zwander.samloaderkotlin.resources.MR
import kotlin.time.ExperimentalTime

val pages = Page::class.sealedSubclasses
    .mapNotNull { it.objectInstance }
    .sortedBy { it.index }

/**
 * Represents the available pages.
 */
sealed class Page(
    val render: @Composable () -> Unit,
    val labelRes: StringResource,
    val index: Int,
) {
    @Keep
    data object Downloader : Page({ DownloadView() }, MR.strings.downloader, 0)
    @Keep
    data object Decrypter : Page({ DecryptView() }, MR.strings.decrypter, 1)
    @Keep
    data object History : Page({ HistoryView() }, MR.strings.history, 2)
    @Keep
    data object SettingsAbout : Page({ SettingsAboutView() }, MR.strings.more, 3)
}

/**
 * Allows the user to switch among the different app pages.
 */
@Composable
internal fun TabView(
    selectedPage: Page,
    onPageSelected: (Page) -> Unit,
    indicator: @Composable ((List<TabPosition>) -> Unit),
) {
    TabRow(
        modifier = Modifier.fillMaxWidth()
            .height(48.dp),
        selectedTabIndex = selectedPage.index,
        indicator = indicator,
        divider = {},
    ) {
        pages.forEach { page ->
            Tab(
                selected = selectedPage == page,
                text = {
                    Text(
                        text = page.labelRes(),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                onClick = {
                    onPageSelected(page)
                },
            )
        }
    }
}
