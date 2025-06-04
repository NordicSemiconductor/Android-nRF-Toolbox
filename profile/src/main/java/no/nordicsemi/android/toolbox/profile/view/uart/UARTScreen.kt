package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.data.UARTServiceData
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent

@Composable
internal fun UARTScreen(
    state: UARTServiceData,
    onEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    var isMacroOpen by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UARTContentView(state, onEvent)
        // Spacer that pushes content to bottom
        Spacer(modifier = Modifier.weight(1f))
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isMacroOpen = !isMacroOpen }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Macro")
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (isMacroOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
        }

        if (isMacroOpen) {
            MacroBottomSheet(state, onDismiss = { isMacroOpen = false }) { onEvent(it) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MacroBottomSheet(
    uartServiceData: UARTServiceData,
    onDismiss: () -> Unit,
    onEvent: (DeviceConnectionViewEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
            // do nothing, we want to keep the state
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 16.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .width(50.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    ) {
        MacroSection(uartServiceData.uartViewState, onEvent)
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
    onEvent: (DeviceConnectionViewEvent) -> Unit,
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