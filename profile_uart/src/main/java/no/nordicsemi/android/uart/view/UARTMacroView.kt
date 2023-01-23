/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.uart.view

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.UARTConfiguration
import no.nordicsemi.android.uart.data.UARTMacro

private val divider = 4.dp

@Composable
internal fun UARTMacroView(
    configuration: UARTConfiguration,
    isEdited: Boolean,
    onEvent: (UARTViewEvent) -> Unit
) {
    BoxWithConstraints {
        val buttonSize = if (maxWidth < 260.dp) {
            48.dp //Minimum touch area
        }  else {
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
    onEvent: (UARTViewEvent) -> Unit
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
    buttonSize: Dp,
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
