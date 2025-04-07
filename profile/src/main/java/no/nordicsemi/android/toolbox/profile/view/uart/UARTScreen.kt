package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.data.UARTServiceData
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent

@Composable
internal fun UARTScreen(
    state: UARTServiceData,
    onEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }) {
        MacroSection(state.uartViewState, onEvent)
        Spacer(modifier = Modifier.weight(1f))
        UARTContentView(state, onEvent)

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
        OutlinedCard {
            InputSection(onEvent = onEvent)
            HorizontalDivider()
            OutputSection(state.messages) {
                onEvent(it)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UARTContentViewPreview() {
    UARTContentView(UARTServiceData()) {}
}