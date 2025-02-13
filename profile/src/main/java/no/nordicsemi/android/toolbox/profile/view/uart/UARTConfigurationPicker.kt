package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.UARTViewState
import no.nordicsemi.android.toolbox.profile.data.uart.UARTConfiguration
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import timber.log.Timber

@Composable
internal fun UARTConfigurationPicker(
    state: UARTViewState,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    val showDialog = rememberSaveable { mutableStateOf(false) }

    UARTConfigurationButton(state.selectedConfiguration) {
        showDialog.value = true
    }

    if (showDialog.value) {
        // TODO: Show dialog
        Timber.d("Show dialog")
    }
}

@Preview(showBackground = true)
@Composable
private fun UARTConfigurationPickerPreview() {
    UARTConfigurationPicker(
        state = UARTViewState(
            configurations = listOf(
                UARTConfiguration(1, "Config 1"),
                UARTConfiguration(2, "Config 2"),
                UARTConfiguration(3, "Config 3"),
            ),
        )
    ) {}
}

@Composable
internal fun UARTConfigurationButton(configuration: UARTConfiguration?, onClick: () -> Unit) {
    OutlinedButton(onClick = { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.uart_configuration_picker_hint),
                    style = MaterialTheme.typography.labelSmall
                )
                val text = configuration?.name
                    ?: stringResource(id = R.string.uart_configuration_picker_not_selected)
                Text(text = text, style = MaterialTheme.typography.bodyMedium)
            }

            Icon(Icons.Default.ArrowDropDown, contentDescription = "")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UARTConfigurationButtonPreview() {
    UARTConfigurationButton(
        configuration = UARTConfiguration(1, "Config 1"),
        onClick = {}
    )
}
