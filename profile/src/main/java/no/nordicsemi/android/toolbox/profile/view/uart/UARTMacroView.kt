package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.uart.UARTConfiguration
import no.nordicsemi.android.toolbox.profile.data.uart.UARTMacro
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent

private val divider = 4.dp

@Composable
internal fun UARTMacroView(
    configuration: UARTConfiguration,
    isEdited: Boolean,
    onEvent: (UARTEvent) -> Unit
) {
    BoxWithConstraints {
        val buttonSize = if (maxWidth < 260.dp) {
            48.dp //Minimum touch area
        } else {
            80.dp
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(divider),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(divider)
            ) {
                Item(configuration, isEdited, 0, buttonSize, onEvent)
                Item(configuration, isEdited, 1, buttonSize, onEvent)
                Item(configuration, isEdited, 2, buttonSize, onEvent)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(divider)
            ) {
                Item(configuration, isEdited, 3, buttonSize, onEvent)
                Item(configuration, isEdited, 4, buttonSize, onEvent)
                Item(configuration, isEdited, 5, buttonSize, onEvent)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(divider)
            ) {
                Item(configuration, isEdited, 6, buttonSize, onEvent)
                Item(configuration, isEdited, 7, buttonSize, onEvent)
                Item(configuration, isEdited, 8, buttonSize, onEvent)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UARTMacroViewPreview() {
    UARTMacroView(
        configuration = UARTConfiguration(1, "Config 1"),
        isEdited = false
    ) {}
}

@Composable
private fun Item(
    configuration: UARTConfiguration,
    isEdited: Boolean,
    position: Int,
    buttonSize: Dp,
    onEvent: (UARTEvent) -> Unit
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
    onEvent: (UARTEvent) -> Unit
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
                    onEvent(UARTEvent.OnEditMacro(position))
                } else {
                    onEvent(UARTEvent.OnRunMacro(macro))
                }
            }
            .background(getBackground(isEdited, macro))
    )
}

@Composable
private fun EmptyButton(
    isEdited: Boolean,
    position: Int,
    buttonSize: Dp,
    onEvent: (UARTEvent) -> Unit
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

@Preview(showBackground = true)
@Composable
private fun EmptyButtonPreview() {
    NordicTheme {
        EmptyButton(false, 0, 80.dp) {}
    }
}

@Composable
private fun getBackground(isEdited: Boolean, macro: UARTMacro? = null): Color {
    return when {
        !isEdited && macro != null -> {
            MaterialTheme.colorScheme.primary
        }

        isEdited && macro == null -> {
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        }

        macro == null -> {
            ButtonDefaults.buttonColors().disabledContainerColor
        }

        else -> {
            MaterialTheme.colorScheme.tertiary
        }
    }
}