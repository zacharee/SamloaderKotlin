@file:Suppress("FunctionName", "unused")

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.InterfaceOrientation
import androidx.compose.ui.uikit.LocalInterfaceOrientation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import tk.zwander.commonCompose.MainView
import kotlin.time.ExperimentalTime

@OptIn(InternalComposeUiApi::class, ExperimentalTime::class)
fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        val orientation = LocalInterfaceOrientation.current

        MainView(
            modifier = Modifier
                .imePadding()
                .systemBarsPadding(),
        )
    }
}

val displayBottom: WindowInsets
    @Composable
    @OptIn(InternalComposeUiApi::class)
    get() {
        val size = 16.dp

        return when (LocalInterfaceOrientation.current) {
            InterfaceOrientation.Portrait -> WindowInsets(bottom = size)
            InterfaceOrientation.PortraitUpsideDown -> WindowInsets(top = size)
            InterfaceOrientation.LandscapeLeft -> WindowInsets(left = size)
            InterfaceOrientation.LandscapeRight -> WindowInsets(right = size)
        }
    }
