package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.UARTViewState
import no.nordicsemi.android.toolbox.profile.data.uart.UARTConfiguration
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UARTConfigurationPicker(
    state: UARTViewState,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }

    ) {
        OutlinedButton(
            onClick = { },
            modifier = Modifier.menuAnchor(PrimaryNotEditable)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val title =
                    state.selectedConfigurationName
                        ?: stringResource(id = R.string.uart_configuration_picker_hint)
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Icon(Icons.Default.ArrowDropDown, contentDescription = "")
            }
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            state.configurations.forEach { configuration ->
                DropdownMenuItem(
                    text = { Text(text = configuration.name) },
                    onClick = {
                        onEvent(UARTEvent.OnConfigurationSelected(configuration))
                        expanded = false
                    },
                )
            }
        }

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
