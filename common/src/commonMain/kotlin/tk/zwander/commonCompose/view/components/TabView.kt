@file:OptIn(DangerousInternalIoApi::class, ExperimentalTime::class)

package tk.zwander.commonCompose.view.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import io.ktor.utils.io.core.internal.DangerousInternalIoApi
import korlibs.io.annotations.Keep
import tk.zwander.commonCompose.view.LocalUseMicaEffect
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
    val iconRes: ImageResource,
    val index: Int,
) {
    @Keep
    data object Downloader : Page({ DownloadView() }, MR.strings.downloader, MR.images.download, 0)

    @Keep
    data object Decrypter :
        Page({ DecryptView() }, MR.strings.decrypter, MR.images.lock_open_outline, 1)

    @Keep
    data object History : Page({ HistoryView() }, MR.strings.history, MR.images.history, 2)

    @Keep
    data object SettingsAbout : Page({ SettingsAboutView() }, MR.strings.more, MR.images.cog, 3)
}

/**
 * Allows the user to switch among the different app pages.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun TabView(
    selectedPage: Int,
    onPageSelected: (Int) -> Unit,
) {
    val density = LocalDensity.current
    var anyEllipsis by remember(density.density, density.fontScale) {
        mutableStateOf(false)
    }

    TabRow(
        modifier = Modifier.fillMaxWidth()
            .height(48.dp),
        selectedTabIndex = selectedPage,
        divider = {},
        containerColor = if (LocalUseMicaEffect.current) Color.Transparent else TabRowDefaults.containerColor,
    ) {
        pages.forEach { page ->
            val text: (@Composable () -> Unit)? = if (anyEllipsis) null else {
                {
                    Text(
                        text = stringResource(page.labelRes),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        onTextLayout = {
                            if (!anyEllipsis) {
                                anyEllipsis = it.isLineEllipsized(0)
                            }
                        },
                    )
                }
            }

            val icon: (@Composable () -> Unit)? = if (anyEllipsis) {
                {
                    Icon(
                        painter = painterResource(page.iconRes),
                        contentDescription = stringResource(page.labelRes),
                    )
                }
            } else null

            Tab(
                selected = selectedPage == page.index,
                text = text,
                icon = icon,
                onClick = {
                    onPageSelected(page.index)
                },
            )
        }
    }
}
