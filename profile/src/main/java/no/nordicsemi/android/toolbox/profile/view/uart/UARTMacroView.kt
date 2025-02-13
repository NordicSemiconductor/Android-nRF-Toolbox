package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.uart.UARTConfiguration
import no.nordicsemi.android.toolbox.profile.data.uart.UARTMacro
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent

private val divider = 4.dp

@Composable
internal fun UARTMacroView(
    configuration: UARTConfiguration,
    isEdited: Boolean,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    BoxWithConstraints {
        val buttonSize = if (maxWidth < 260.dp) {
            48.dp //Minimum touch area
        } else {
            80.dp
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            Row {
                Item(configuration, isEdited, 0, buttonSize, onEvent)
                Spacer(modifier = Modifier.size(divider))
                Item(configuration, isEdited, 1, buttonSize, onEvent)
                Spacer(modifier = Modifier.size(divider))
                Item(configuration, isEdited, 2, buttonSize, onEvent)
            }

            Spacer(modifier = Modifier.size(divider))

            Row {
                Item(configuration, isEdited, 3, buttonSize, onEvent)
                Spacer(modifier = Modifier.size(divider))
                Item(configuration, isEdited, 4, buttonSize, onEvent)
                Spacer(modifier = Modifier.size(divider))
                Item(configuration, isEdited, 5, buttonSize, onEvent)
            }

            Spacer(modifier = Modifier.size(divider))

            Row {
                Item(configuration, isEdited, 6, buttonSize, onEvent)
                Spacer(modifier = Modifier.size(divider))
                Item(configuration, isEdited, 7, buttonSize, onEvent)
                Spacer(modifier = Modifier.size(divider))
                Item(configuration, isEdited, 8, buttonSize, onEvent)
            }
        }
    }
}

@Composable
private fun Item(
    configuration: UARTConfiguration,
    isEdited: Boolean,
    position: Int,
    buttonSize: Dp,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    val macro = configuration.macros.getOrNull(position)

    if (macro == null) {
        EmptyButton(isEdited, position, buttonSize, onEvent)
    } else {
        MacroButton(macro, position, isEdited, buttonSize, onEvent)
    }
}

@Composable
private fun MacroButton(
    macro: UARTMacro,
    position: Int,
    isEdited: Boolean,
    buttonSize: Dp,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Image(
        painter = painterResource(id = macro.icon.index),
        contentDescription = stringResource(id = R.string.uart_macro_icon),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
        modifier = Modifier
            .size(buttonSize)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                if (isEdited) {
                    onEvent(UARTEvent.OnEditMacro(position))
                } else {
                    onEvent(UARTEvent.OnRunMacro(macro))
                }
            }
            .background(getBackground(isEdited))
    )
}

@Composable
private fun EmptyButton(
    isEdited: Boolean,
    position: Int,
    buttonSize: Dp,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                if (isEdited) {
                    onEvent(UARTEvent.OnEditMacro(position))
                }
            }
            .background(getBackground(isEdited))
    )
}

@Preview
@Composable
private fun EmptyButtonPreview() {
    EmptyButton(false, 0, 80.dp) {}
}

@Composable
private fun getBackground(isEdited: Boolean): Color {
    return if (!isEdited) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
}