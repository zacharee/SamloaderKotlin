package tk.zwander.commonCompose.view.components.settingsitems

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecyclePauseOrDisposeEffectResult
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import tk.zwander.common.data.IOptionItem
import tk.zwander.commonCompose.util.collectAsImmediateMutableState
import tk.zwander.commonCompose.view.components.LabelDesc
import tk.zwander.commonCompose.view.components.TransparencyCard

@Composable
fun BooleanPreference(
    item: IOptionItem.BasicOptionItem.BooleanItem,
    modifier: Modifier = Modifier,
) {
    val lifecycle = LocalLifecycleOwner.current
    var state by item.key.asMutableStateFlow().collectAsImmediateMutableState()

    LaunchedEffect(state) {
        if (state) {
            item.onEnabledAction()
        }
    }

    LifecycleResumeEffect(null, lifecycle) {
        if (lifecycle.lifecycle.currentState == Lifecycle.State.RESUMED) {
            if (state || !item.validator()) {
                state = false
            }
        }

        object : LifecyclePauseOrDisposeEffectResult {
            override fun runPauseOrOnDisposeEffect() {}
        }
    }

    TransparencyCard(
        modifier = modifier,
        onClick = {
            state = !state
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelDesc(
                item = item,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )

            Switch(
                checked = state,
                onCheckedChange = {
                    state = it
                },
            )
        }
    }
}
