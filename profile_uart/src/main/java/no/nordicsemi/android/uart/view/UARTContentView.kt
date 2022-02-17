package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.android.theme.view.SectionTitle
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.UARTData
import no.nordicsemi.android.uart.data.UARTMacro

@Composable
internal fun UARTContentView(state: UARTData, macros: List<UARTMacro>, onEvent: (UARTViewEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        OutputSection(state.text)

        Spacer(modifier = Modifier.height(16.dp))

        InputSection(macros, onEvent)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onEvent(DisconnectEvent) }
        ) {
            Text(text = stringResource(id = R.string.disconnect))
        }
    }
}

@Composable
private fun InputSection(macros: List<UARTMacro>, onEvent: (UARTViewEvent) -> Unit) {
    val showSearchDialog = remember { mutableStateOf(false) }

    if (showSearchDialog.value) {
        UARTAddMacroDialog(onDismiss = { showSearchDialog.value = false }, onEvent = onEvent)
    }

    ScreenSection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle(resId = R.drawable.ic_input, title = stringResource(R.string.uart_input))

            Spacer(modifier = Modifier.height(16.dp))

            macros.forEach {
                MacroItem(macro = it, onEvent = onEvent)

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (macros.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.uart_no_macros_info),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { showSearchDialog.value = true }
            ) {
                Text(text = stringResource(id = R.string.uart_add_macro))
            }
        }
    }
}

@Composable
private fun OutputSection(text: String) {
    ScreenSection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle(resId = R.drawable.ic_output, title = stringResource(R.string.uart_output))

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = text.ifBlank { stringResource(id = R.string.uart_output_placeholder) })
        }
    }
}
