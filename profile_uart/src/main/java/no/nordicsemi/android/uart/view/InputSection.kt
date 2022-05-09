package no.nordicsemi.android.uart.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.material.you.*
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
            TextField(text = text.value, hint = hint) {
                text.value = it
            }
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
