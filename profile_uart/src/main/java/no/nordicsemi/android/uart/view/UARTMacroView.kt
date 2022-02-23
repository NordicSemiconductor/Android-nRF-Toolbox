package no.nordicsemi.android.uart.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.UARTConfiguration
import no.nordicsemi.android.uart.data.UARTMacro

private val divider = 4.dp
private val buttonSize = 80.dp

@Composable
internal fun UARTMacroView(
    configuration: UARTConfiguration,
    isEdited: Boolean,
    onEvent: (UARTViewEvent) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        Row {
            Item(configuration, isEdited, 0, onEvent)
            Spacer(modifier = Modifier.size(divider))
            Item(configuration, isEdited, 1, onEvent)
            Spacer(modifier = Modifier.size(divider))
            Item(configuration, isEdited, 2, onEvent)
        }

        Spacer(modifier = Modifier.size(divider))

        Row {
            Item(configuration, isEdited, 3, onEvent)
            Spacer(modifier = Modifier.size(divider))
            Item(configuration, isEdited, 4, onEvent)
            Spacer(modifier = Modifier.size(divider))
            Item(configuration, isEdited, 5, onEvent)
        }

        Spacer(modifier = Modifier.size(divider))

        Row {
            Item(configuration, isEdited, 6, onEvent)
            Spacer(modifier = Modifier.size(divider))
            Item(configuration, isEdited, 7, onEvent)
            Spacer(modifier = Modifier.size(divider))
            Item(configuration, isEdited, 8, onEvent)
        }
    }
}

@Composable
private fun Item(
    configuration: UARTConfiguration,
    isEdited: Boolean,
    position: Int,
    onEvent: (UARTViewEvent) -> Unit
) {
    val macro = configuration.macros.getOrNull(position)

    if (macro == null) {
        EmptyButton(isEdited, position, onEvent)
    } else {
        MacroButton(macro, position, isEdited, onEvent)
    }
}

@Composable
private fun MacroButton(
    macro: UARTMacro,
    position: Int,
    isEdited: Boolean,
    onEvent: (UARTViewEvent) -> Unit
) {
    Image(
        painter = painterResource(id = macro.icon.toResId()),
        contentDescription = stringResource(id = R.string.uart_macro_icon),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
        modifier = Modifier
            .size(buttonSize)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                if (isEdited) {
                    onEvent(OnEditMacro(position))
                } else {
                    onEvent(OnRunMacro(macro))
                }
            }
            .background(getBackground(isEdited))
    )
}

@Composable
private fun EmptyButton(
    isEdited: Boolean,
    position: Int,
    onEvent: (UARTViewEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                if (isEdited) {
                    onEvent(OnEditMacro(position))
                }
            }
            .background(getBackground(isEdited))
    )
}

@Composable
private fun getBackground(isEdited: Boolean): Color {
    return if (!isEdited) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
}
