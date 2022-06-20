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

import no.nordicsemi.android.uart.data.MacroEol
import no.nordicsemi.android.uart.data.UARTConfiguration
import no.nordicsemi.android.uart.data.UARTMacro

internal sealed class UARTViewEvent

internal data class OnEditMacro(val position: Int) : UARTViewEvent()
internal data class OnCreateMacro(val macro: UARTMacro) : UARTViewEvent()
internal object OnDeleteMacro : UARTViewEvent()
internal object OnEditFinish : UARTViewEvent()

internal data class OnConfigurationSelected(val configuration: UARTConfiguration) : UARTViewEvent()
internal data class OnAddConfiguration(val name: String) : UARTViewEvent()
internal object OnEditConfiguration : UARTViewEvent()
internal object OnDeleteConfiguration : UARTViewEvent()
internal data class OnRunMacro(val macro: UARTMacro) : UARTViewEvent()
internal data class OnRunInput(val text: String, val newLineChar: MacroEol) : UARTViewEvent()

internal object ClearOutputItems : UARTViewEvent()
internal object DisconnectEvent : UARTViewEvent()

internal object NavigateUp : UARTViewEvent()
internal object OpenLogger : UARTViewEvent()

internal object MacroInputSwitchClick : UARTViewEvent()
