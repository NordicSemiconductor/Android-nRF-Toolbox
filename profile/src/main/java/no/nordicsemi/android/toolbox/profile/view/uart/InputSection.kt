package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.uart.MacroEol
import no.nordicsemi.android.toolbox.profile.viewmodel.ProfileUiEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent

@Composable
internal fun InputSection(
    onEvent: (ProfileUiEvent) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val checkedItem by rememberSaveable { mutableStateOf(MacroEol.entries[0]) }
    var isEmptyText: Boolean by rememberSaveable { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = text,
                textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                onValueChange = { newValue ->
                    text = newValue
                    isEmptyText = false
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant)
            )
            if (text.isEmpty() && !isEmptyText) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterStart),
                    text = stringResource(id = R.string.uart_input_hint),
                )
            } else if (isEmptyText) {
                Text(
                    text = "Input cannot be empty.",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        Icon(
            if (isEmptyText) Icons.Default.Error else Icons.AutoMirrored.Filled.Send,
            contentDescription = stringResource(id = R.string.uart_input_macro),
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    if (text.isNotEmpty()) {
                        onEvent(UARTEvent.OnRunInput(text, checkedItem))
                        text = ""
                    } else {
                        isEmptyText = true
                    }
                }
                .padding(8.dp),
            tint = if (isEmptyText) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InputSectionPreview() {
    InputSection(
        onEvent = {})
}
