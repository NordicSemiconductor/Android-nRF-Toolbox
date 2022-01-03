package no.nordicsemi.android.uart.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.material.you.Card
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.UARTMacro

@Composable
internal fun MacroItem(macro: UARTMacro, onEvent: (UARTViewEvent) -> Unit) {
    Card(backgroundColor = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(id = R.string.uart_run_macro_description),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onEvent(OnRunMacro(macro)) }
            )

            Spacer(modifier = Modifier.padding(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = macro.alias,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(id = R.string.uart_command_field, macro.command),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.padding(16.dp))

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.uart_delete_macro_description),
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onEvent(OnDeleteMacro(macro)) }
            )
        }
    }
}
