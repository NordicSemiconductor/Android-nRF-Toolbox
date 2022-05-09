package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.material.you.ScreenSection
import no.nordicsemi.android.uart.data.UARTData

@Composable
internal fun UARTContentView(
    state: UARTData,
    onEvent: (UARTViewEvent) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {

        ScreenSection(modifier = Modifier.weight(1f)) {
            OutputSection(state.displayMessages, onEvent)
        }

        Spacer(modifier = Modifier.size(16.dp))

        InputSection(onEvent = onEvent)
    }
}
