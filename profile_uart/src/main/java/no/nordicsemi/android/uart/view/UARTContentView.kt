package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.material.you.ScreenSection
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.UARTData

@Composable
internal fun UARTContentView(
    state: UARTData,
    viewState: UARTViewState,
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

        if (viewState.isInputVisible) {
            InputSection(onEvent = onEvent)
        } else {
            MacroSection(viewState, onEvent)
        }

        Spacer(modifier = Modifier.size(16.dp))

        Button(
            onClick = { onEvent(DisconnectEvent) }
        ) {
            Text(text = stringResource(id = R.string.disconnect))
        }
    }
}
