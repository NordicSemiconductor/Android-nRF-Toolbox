package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.data.UARTServiceData
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.ui.view.ScreenSection

@Composable
internal fun UARTScreen(
    state: UARTServiceData,
    onEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        UARTContentView(state, onEvent)
        Spacer(modifier = Modifier.weight(1f))
        MacroSection(state.uartViewState, onEvent)

    }
}

@Preview(showBackground = true)
@Composable
private fun UARTScreenPreview() {
    UARTScreen(UARTServiceData()) {}
}


@Composable
private fun UARTContentView(
    state: UARTServiceData,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenSection {
            OutputSection(state.messages) {
                onEvent(it)
            }
            InputSection(onEvent = onEvent)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UARTContentViewPreview() {
    UARTContentView(UARTServiceData()) {}
}