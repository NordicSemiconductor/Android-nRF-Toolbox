package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
internal fun UARTContentView(state: UARTData, viewState: UARTViewState, onEvent: (UARTViewEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        OutputSection(state.text)

        Spacer(modifier = Modifier.height(16.dp))

        InputSection(viewState, onEvent)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onEvent(DisconnectEvent) }
        ) {
            Text(text = stringResource(id = R.string.disconnect))
        }
    }
}

@Composable
private fun InputSection(viewState: UARTViewState, onEvent: (UARTViewEvent) -> Unit) {
    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value) {
        UARTAddConfigurationDialog(onEvent)
    }

    ScreenSection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle(resId = R.drawable.ic_input, title = stringResource(R.string.uart_input))

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Box(modifier = Modifier.weight(1f)) {
                    UARTConfigurationPicker(viewState, onEvent)
                }
                
                IconButton(onClick = { showDialog.value = true }) {
                    Icon(Icons.Default.Add, stringResource(id = R.string.uart_configuration_add))
                }

                viewState.selectedConfiguration?.let {

                    IconButton(onClick = { onEvent(OnEditConfiguration) }) {
                        Icon(Icons.Default.Edit, stringResource(id = R.string.uart_configuration_edit))
                    }

                    IconButton(onClick = { onEvent(OnDeleteConfiguration) }) {
                        Icon(Icons.Default.Delete, stringResource(id = R.string.uart_configuration_delete))
                    }
                }
            }

            viewState.selectedConfiguration?.let {
                Spacer(modifier = Modifier.height(16.dp))

                UARTMacroView(it, viewState.isConfigurationEdited, onEvent)
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
