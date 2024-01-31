@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE")
@file:JvmName("AlertDialogSkia")

package tk.zwander.commonCompose.view.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.onClick
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.scene.rememberComposeSceneLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.AlignmentOffsetPositionProvider
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import tk.zwander.commonCompose.view.LocalMenuBarHeight
import kotlin.jvm.JvmName

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal actual fun PlatformAlertDialog(
    showing: Boolean,
    onDismissRequest: () -> Unit,
    buttons: @Composable RowScope.() -> Unit,
    modifier: Modifier,
    title: (@Composable () -> Unit)?,
    text: (@Composable ColumnScope.() -> Unit)?,
    shape: Shape,
    backgroundColor: Color,
    contentColor: Color
) {
    val alpha by animateFloatAsState(if (showing) 1f else 0f, spring(stiffness = Spring.StiffnessMediumLow))
    val showingForAnimation by derivedStateOf {
        alpha > 0f
    }

    if (showing || showingForAnimation) {
        AbsolutePopup(
            alignment = Alignment.Center,
            focusable = true,
            onPreviewKeyEvent = {
                if (it.key == Key.Escape) {
                    onDismissRequest()
                    true
                } else {
                    false
                }
            },
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f * alpha))
                    .fillMaxSize()
                    .onClick { onDismissRequest() }
                    .padding(top = 16.dp + LocalMenuBarHeight.current, bottom = 16.dp)
                    .alpha(alpha),
                contentAlignment = Alignment.Center
            ) {
                with (LocalDensity.current) {
                    AlertDialogContents(
                        buttons,
                        modifier.then(
                            Modifier.widthIn(
                                max = minOf(
                                    constraints.maxWidth.toDp() - 32.dp,
                                    400.dp
                                )
                            ).onClick {
                                // To prevent the Box's onClick consuming clicks on the dialog itself.
                            }.animateContentSize()
                        ),
                        title,
                        text,
                        shape,
                        backgroundColor,
                        contentColor
                    )
                }
            }
        }
    }
}

@Composable
internal fun AbsolutePopup(
    alignment: Alignment = Alignment.TopStart,
    offset: IntOffset = IntOffset(0, 0),
    focusable: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable () -> Unit
) {
    val popupPositioner = remember(alignment, offset) {
        AlignmentOffsetPositionProvider(
            alignment,
            offset
        )
    }

    AbsolutePopup(
        provider = popupPositioner,
        focusable = focusable,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        content = content
    )
}

@OptIn(InternalComposeUiApi::class)
@Composable
internal fun AbsolutePopup(
    provider: PopupPositionProvider,
    focusable: Boolean = false,
    onPreviewKeyEvent: ((KeyEvent) -> Boolean) = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val layer = rememberComposeSceneLayer(focusable = focusable)

    var popupBounds by remember { mutableStateOf(IntRect.Zero) }
    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(null) {
        focusRequester.requestFocus()
    }

    layer.setContent {
        Layout(
            content = content,
            measurePolicy = { measureables, constraints ->
                val width = constraints.maxWidth
                val height = constraints.maxHeight

                layout(constraints.maxWidth, constraints.maxHeight) {
                    measureables.forEach {
                        val placeable = it.measure(constraints)
                        val position = provider.calculatePosition(
                            anchorBounds = IntRect(0, 0, width, height),
                            windowSize = IntSize(width, height),
                            layoutDirection = layoutDirection,
                            popupContentSize = IntSize(placeable.width, placeable.height)
                        )

                        popupBounds = IntRect(
                            position,
                            IntSize(placeable.width, placeable.height)
                        )

                        layer.boundsInWindow = popupBounds
                        placeable.place(position.x, position.y)
                    }
                }
            },
            modifier = Modifier
                .onPreviewKeyEvent(onPreviewKeyEvent)
                .onKeyEvent(onKeyEvent)
                .focusRequester(focusRequester),
        )
    }
}
