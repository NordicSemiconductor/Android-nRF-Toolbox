package no.nordicsemi.android.uart.view

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
import no.nordicsemi.android.theme.view.dialog.*
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.UARTConfiguration
import no.nordicsemi.android.utils.exhaustive

@Composable
internal fun UARTConfigurationPicker(state: UARTViewState, onEvent: (UARTViewEvent) -> Unit) {
    val showDialog = rememberSaveable { mutableStateOf(false) }

    UARTConfigurationButton(state.selectedConfiguration) {
        showDialog.value = true
    }

    if (showDialog.value) {
        SelectWheelSizeDialog(state) {
            when (it) {
                FlowCanceled -> showDialog.value = false
                is ItemSelectedResult -> {
                    onEvent(OnConfigurationSelected(state.configurations[it.index]))
                    showDialog.value = false
                }
            }.exhaustive
        }
    }
}

@Composable
internal fun SelectWheelSizeDialog(state: UARTViewState, onEvent: (StringListDialogResult) -> Unit) {
    val wheelEntries = state.configurations.map { it.name }

    StringListDialog(createConfig(wheelEntries) {
        onEvent(it)
    })
}

@Composable
private fun createConfig(entries: List<String>, onResult: (StringListDialogResult) -> Unit): StringListDialogConfig {
    return StringListDialogConfig(
        title = stringResource(id = R.string.uart_configuration_picker_dialog).toAnnotatedString(),
        items = entries,
        onResult = onResult,
        leftIcon = R.drawable.ic_uart_settings
    )
}

@Composable
internal fun UARTConfigurationButton(configuration: UARTConfiguration?, onClick: () -> Unit) {
    OutlinedButton(onClick = { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Column {
                Text(
                    text = stringResource(id = R.string.uart_configuration_picker_hint),
                    style = MaterialTheme.typography.labelSmall
                )
                val text = configuration?.name ?: stringResource(id = R.string.uart_configuration_picker_not_selected)
                Text(text = text, style = MaterialTheme.typography.bodyMedium)
            }

            Icon(Icons.Default.ArrowDropDown, contentDescription = "")
        }
    }
}
