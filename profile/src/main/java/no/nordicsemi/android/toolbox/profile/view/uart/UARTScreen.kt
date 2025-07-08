package no.nordicsemi.android.toolbox.profile.view.uart

import android.annotation.SuppressLint
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.data.UARTServiceData
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent

@Composable
internal fun UARTScreen(
    state: UARTServiceData,
    onEvent: (UARTEvent) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value) {
            scrollState.animateScrollTo(scrollState.maxValue, tween(300))
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), state = lazyListState) {
        item { MacroSection(state.uartViewState, onEvent) }

        item { Spacer(Modifier.size(16.dp)) }

        item { UARTContentView(state, onEvent) }
    }
}

@Preview(showBackground = true)
@Composable
private fun UARTScreenPreview() {
    UARTScreen(UARTServiceData()) {}
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun UARTContentView(
    state: UARTServiceData,
    onEvent: (UARTEvent) -> Unit,
) {
    OutputSection(
        records = state.messages,
        onEvent = onEvent
    )

}