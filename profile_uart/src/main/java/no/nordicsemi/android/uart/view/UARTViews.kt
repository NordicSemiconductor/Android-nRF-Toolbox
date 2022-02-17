package no.nordicsemi.android.uart.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(id = R.string.uart_run_macro_description),
                modifier = Modifier
                    .size(70.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onEvent(OnRunMacro(macro)) }
            )

            Spacer(modifier = Modifier.size(16.dp))

            Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(
                    text = stringResource(id = R.string.uart_macro_dialog_selected_eol, macro.newLineChar.toDisplayString()),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = stringResource(id = R.string.uart_command_field, macro.command),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.uart_delete_macro_description),
                modifier = Modifier
                    .padding(8.dp)
                    .padding(end = 8.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onEvent(OnDeleteMacro(macro)) }
            )
        }
    }
}
