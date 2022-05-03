package no.nordicsemi.android.uart.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.nordicsemi.android.material.you.*
import no.nordicsemi.android.theme.view.SectionTitle
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.MacroEol
import no.nordicsemi.android.uart.data.UARTData
import no.nordicsemi.android.uart.data.UARTRecord
import no.nordicsemi.android.uart.data.UARTRecordType
import no.nordicsemi.android.utils.EMPTY
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun UARTContentView(
    state: UARTData,
    viewState: UARTViewState,
    onEvent: (UARTViewEvent) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {

        ScreenSection(modifier = Modifier.weight(1f)) {
            OutputSection(state.displayMessages, onEvent)
        }

        Spacer(modifier = Modifier.size(16.dp))

//        MacroSection(viewState, onEvent)
//
//        Spacer(modifier = Modifier.size(16.dp))

        InputSection(onEvent = onEvent)

        Spacer(modifier = Modifier.size(16.dp))

        Button(
            onClick = { onEvent(DisconnectEvent) }
        ) {
            Text(text = stringResource(id = R.string.disconnect))
        }
    }
}

@Composable
private fun InputSection(onEvent: (UARTViewEvent) -> Unit) {
    val text = rememberSaveable { mutableStateOf(String.EMPTY) }
    val hint = stringResource(id = R.string.uart_input_hint)
    val checkedItem = rememberSaveable { mutableStateOf(MacroEol.values()[0]) }

    val items = MacroEol.values().map {
        RadioButtonItem(it.toDisplayString(), it == checkedItem.value)
    }
    val viewEntity = RadioGroupViewEntity(items)

    ScreenSection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle(resId = R.drawable.ic_input, title = stringResource(R.string.uart_input))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.uart_macro_dialog_eol),
                    style = MaterialTheme.typography.labelLarge
                )

                RadioButtonGroup(viewEntity) {
                    val i = items.indexOf(it)
                    checkedItem.value = MacroEol.values()[i]
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    TextField(text = text.value, hint = hint) {
                        text.value = it
                    }
                }

                Spacer(modifier = Modifier.size(16.dp))

                Button(
                    onClick = { onEvent(OnRunInput(text.value, checkedItem.value)) },
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(text = stringResource(id = R.string.uart_send))
                }
            }
        }
    }
}

@Composable
private fun MacroSection(viewState: UARTViewState, onEvent: (UARTViewEvent) -> Unit) {
    val showAddDialog = rememberSaveable { mutableStateOf(false) }
    val showDeleteDialog = rememberSaveable { mutableStateOf(false) }

    if (showAddDialog.value) {
        UARTAddConfigurationDialog(onEvent) { showAddDialog.value = false }
    }

    if (showDeleteDialog.value) {
        DeleteConfigurationDialog(onEvent) { showDeleteDialog.value = false }
    }

    ScreenSection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle(resId = R.drawable.ic_input, title = stringResource(R.string.uart_macros))

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Box(modifier = Modifier.weight(1f)) {
                    UARTConfigurationPicker(viewState, onEvent)
                }

                IconButton(onClick = { showAddDialog.value = true }) {
                    Icon(Icons.Default.Add, stringResource(id = R.string.uart_configuration_add))
                }

                viewState.selectedConfiguration?.let {

                    if (!viewState.isConfigurationEdited) {
                        IconButton(onClick = { onEvent(OnEditConfiguration) }) {
                            Icon(
                                Icons.Default.Edit,
                                stringResource(id = R.string.uart_configuration_edit)
                            )
                        }
                    } else {
                        IconButton(onClick = { onEvent(OnEditConfiguration) }) {
                            Icon(
                                painterResource(id = R.drawable.ic_pencil_off),
                                stringResource(id = R.string.uart_configuration_edit)
                            )
                        }
                    }

                    IconButton(onClick = { showDeleteDialog.value = true }) {
                        Icon(
                            Icons.Default.Delete,
                            stringResource(id = R.string.uart_configuration_delete)
                        )
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
private fun DeleteConfigurationDialog(onEvent: (UARTViewEvent) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.uart_delete_dialog_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(text = stringResource(id = R.string.uart_delete_dialog_info))
        },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                onEvent(OnDeleteConfiguration)
            }) {
                Text(text = stringResource(id = R.string.uart_delete_dialog_confirm))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.uart_delete_dialog_cancel))
            }
        }
    )
}

@Composable
private fun OutputSection(records: List<UARTRecord>, onEvent: (UARTViewEvent) -> Unit) {
    val scrollDown = remember { mutableStateOf(true) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle(
                resId = R.drawable.ic_output,
                title = stringResource(R.string.uart_output),
                modifier = Modifier,
                menu = { Menu(scrollDown, onEvent) }
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        val listState = rememberLazyListState()

        LazyColumn(
            userScrollEnabled = !scrollDown.value,
            modifier = Modifier
                .fillMaxWidth(),
            state = listState
        ) {
            if (records.isEmpty()) {
                item { Text(text = stringResource(id = R.string.uart_output_placeholder)) }
            } else {
                records.forEach {
                    item {
                        MessageItem(record = it)

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        LaunchedEffect(records) {
            if (!scrollDown.value || records.isEmpty()) {
                return@LaunchedEffect
            }
            launch {
                listState.scrollToItem(records.lastIndex)
            }
        }
    }
}

@Composable
private fun Menu(scrollDown: MutableState<Boolean>, onEvent: (UARTViewEvent) -> Unit) {
    val icon = when (scrollDown.value) {
        true -> R.drawable.ic_sync_down_off
        false -> R.drawable.ic_sync_down
    }
    Row {
        IconButton(onClick = { scrollDown.value = !scrollDown.value }) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = stringResource(id = R.string.uart_scroll_down)
            )
        }

        IconButton(onClick = { onEvent(ClearOutputItems) }) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.uart_clear_items),
            )
        }
    }
}

@Composable
private fun MessageItem(record: UARTRecord) {
    Column {
        Text(
            text = record.timeToString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = createRecordText(record = record),
            style = MaterialTheme.typography.bodyMedium,
            color = createRecordColor(record = record)
        )
    }
}

@Composable
private fun createRecordText(record: UARTRecord): String {
    return when (record.type) {
        UARTRecordType.INPUT -> stringResource(id = R.string.uart_input_log, record.text)
        UARTRecordType.OUTPUT -> stringResource(id = R.string.uart_output_log, record.text)
    }
}

@Composable
private fun createRecordColor(record: UARTRecord): Color {
    return when (record.type) {
        UARTRecordType.INPUT -> colorResource(id = R.color.nordicGrass)
        UARTRecordType.OUTPUT -> MaterialTheme.colorScheme.onBackground
    }
}

private val datFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.ENGLISH)

private fun UARTRecord.timeToString(): String {
    return datFormatter.format(timestamp)
}
