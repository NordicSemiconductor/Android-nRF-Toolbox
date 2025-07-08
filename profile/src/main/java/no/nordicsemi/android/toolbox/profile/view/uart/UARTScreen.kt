package no.nordicsemi.android.toolbox.profile.view.uart

import android.annotation.SuppressLint
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.data.UARTServiceData
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent

@Composable
internal fun UARTScreen(
    state: UARTServiceData,
    onEvent: (UARTEvent) -> Unit,
) {
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value) {
            scrollState.animateScrollTo(scrollState.maxValue, tween(300))
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MacroSection(state.uartViewState, onEvent)
        UARTContentView(state, onEvent)
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