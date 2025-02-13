package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.RadioButtonGroup
import no.nordicsemi.android.common.ui.view.RadioButtonItem
import no.nordicsemi.android.common.ui.view.RadioGroupViewEntity
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.uart.MacroEol
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InputSection(onEvent: (DeviceConnectionViewEvent) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val checkedItem by rememberSaveable { mutableStateOf(MacroEol.entries[0]) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .imePadding() // Pushes up when keyboard appears
            .imeNestedScroll()
            .padding(horizontal = 4.dp, vertical = 16.dp) // Extra padding for spacing,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = text,
                label = { Text(stringResource(id = R.string.uart_input_hint)) },
                onValueChange = { newValue: String ->
                    text = newValue
                }
            )
        }

        IconButton(
            onClick = {
                onEvent(UARTEvent.OnRunInput(text, checkedItem))
                text = ""
            }
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(id = R.string.uart_input_macro),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InputSectionPreview() {
    InputSection(onEvent = {})
}

@Composable
internal fun EditInputSection(onEvent: (DeviceConnectionViewEvent) -> Unit) {
    val checkedItem = rememberSaveable { mutableStateOf(MacroEol.entries[0]) }

    val items = MacroEol.entries.map {
        RadioButtonItem(it.toString(), it == checkedItem.value)
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
                    IconButton(onClick = { onEvent(UARTEvent.MacroInputSwitchClicked) }) {
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
                    checkedItem.value = MacroEol.entries.toTypedArray()[i]
                }
            }

            Spacer(modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditInputSectionPreview() {
    EditInputSection(onEvent = {})
}
