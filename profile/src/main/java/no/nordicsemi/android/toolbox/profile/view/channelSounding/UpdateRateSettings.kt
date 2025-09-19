package no.nordicsemi.android.toolbox.profile.view.channelSounding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.data.UpdateRate

@Composable
internal fun UpdateRateSettings(
    selectedItem: UpdateRate,
    onItemSelected: (UpdateRate) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
        )
        // Show AlertDialog when isExpanded is true
        AnimatedVisibility(isExpanded) {
            // Your AlertDialog content here
            UpdateRateDialog(
                selectedUpdateRate = selectedItem,
                onConfirmation = onItemSelected,
                onDismiss = { isExpanded = false }
            )
        }
    }
}

@Composable
internal fun UpdateRateDialog(
    selectedUpdateRate: UpdateRate,
    onDismiss: () -> Unit,
    onConfirmation: (UpdateRate) -> Unit,
) {
    val updateOptions = UpdateRate.entries
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(selectedUpdateRate) }

    AlertDialog(
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Change Update Rate",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Select a new ranging update frequency.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                Column(Modifier.selectableGroup()) {
                    updateOptions.forEach { text ->
                        OutlinedCard(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (text == selectedOption),
                                        onClick = { onOptionSelected(text) },
                                        role = Role.RadioButton
                                    )
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = text.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                RadioButton(
                                    selected = (text == selectedOption),
                                    onClick = null // null recommended for accessibility with screen readers
                                )

                            }
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.scrim
                    )
                    Text(text = "Selecting a new rate will cancel the current session and start a new one.")
                }
            }


        },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmation(selectedOption)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant, // looks "muted"
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun UpdateRateDialogPreview() {
    UpdateRateDialog(
        selectedUpdateRate = UpdateRate.NORMAL,
        onConfirmation = {},
        onDismiss = {}
    )
}
