package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.material.you.RadioButtonGroup
import no.nordicsemi.android.material.you.RadioButtonItem
import no.nordicsemi.android.material.you.RadioGroupViewEntity
import no.nordicsemi.android.material.you.ScreenSection
import no.nordicsemi.android.theme.view.SectionTitle
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.MacroEol
import no.nordicsemi.android.utils.EMPTY

@Composable
internal fun InputSection(onEvent: (UARTViewEvent) -> Unit) {
    val text = rememberSaveable { mutableStateOf(String.EMPTY) }
    val hint = stringResource(id = R.string.uart_input_hint)
    val checkedItem = rememberSaveable { mutableStateOf(MacroEol.values()[0]) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .verticalScroll(rememberScrollState()),
                value = text.value,
                label = { Text(hint) },
                onValueChange = { newValue: String ->
                    text.value = newValue
                })
        }

        Spacer(modifier = Modifier.size(16.dp))

        Button(
            onClick = {
                onEvent(OnRunInput(text.value, checkedItem.value))
                text.value = String.EMPTY
            },
            modifier = Modifier.padding(top = 6.dp)
        ) {
            Text(text = stringResource(id = R.string.uart_send))
        }
    }
}

@Composable
internal fun EditInputSection(onEvent: (UARTViewEvent) -> Unit) {
    val checkedItem = rememberSaveable { mutableStateOf(MacroEol.values()[0]) }

    val items = MacroEol.values().map {
        RadioButtonItem(it.toDisplayString(), it == checkedItem.value)
    }
    val viewEntity = RadioGroupViewEntity(items)

    ScreenSection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle(
                resId = R.drawable.ic_input,
                title = stringResource(R.string.uart_input),
                menu = {
                    IconButton(onClick = { onEvent(MacroInputSwitchClick) }) {
                        Icon(
                            painterResource(id = R.drawable.ic_macro),
                            contentDescription = stringResource(id = R.string.uart_input_macro),
                        )
                    }
                }
            )

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
        }
    }
}
