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

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.MacroEol
import no.nordicsemi.android.uart.data.MacroIcon

@Composable
fun MacroEol.toDisplayString(): String {
    return when (this) {
        MacroEol.LF -> stringResource(id = R.string.uart_macro_dialog_lf)
        MacroEol.CR_LF -> stringResource(id = R.string.uart_macro_dialog_cr_lf)
        MacroEol.CR -> stringResource(id = R.string.uart_macro_dialog_cr)
    }
}

@DrawableRes
fun MacroIcon.toResId(): Int {
    return when (this) {
        MacroIcon.LEFT -> R.drawable.ic_uart_left
        MacroIcon.UP -> R.drawable.ic_uart_up
        MacroIcon.RIGHT -> R.drawable.ic_uart_right
        MacroIcon.DOWN -> R.drawable.ic_uart_down
        MacroIcon.SETTINGS -> R.drawable.ic_uart_settings
        MacroIcon.REW -> R.drawable.ic_uart_rewind
        MacroIcon.PLAY -> R.drawable.ic_uart_play
        MacroIcon.PAUSE -> R.drawable.ic_uart_pause
        MacroIcon.STOP -> R.drawable.ic_uart_stop
        MacroIcon.FWD -> R.drawable.ic_uart_forward
        MacroIcon.INFO -> R.drawable.ic_uart_about
        MacroIcon.NUMBER_1 -> R.drawable.ic_uart_1
        MacroIcon.NUMBER_2 -> R.drawable.ic_uart_2
        MacroIcon.NUMBER_3 -> R.drawable.ic_uart_3
        MacroIcon.NUMBER_4 -> R.drawable.ic_uart_4
        MacroIcon.NUMBER_5 -> R.drawable.ic_uart_5
        MacroIcon.NUMBER_6 -> R.drawable.ic_uart_6
        MacroIcon.NUMBER_7 -> R.drawable.ic_uart_7
        MacroIcon.NUMBER_8 -> R.drawable.ic_uart_8
        MacroIcon.NUMBER_9 -> R.drawable.ic_uart_9
    }
}
