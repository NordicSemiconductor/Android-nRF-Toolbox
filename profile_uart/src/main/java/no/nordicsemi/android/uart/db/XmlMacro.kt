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
package no.nordicsemi.android.uart.db

import no.nordicsemi.android.uart.data.MacroEol
import no.nordicsemi.android.uart.data.MacroIcon
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

//import no.nordicsemi.android.uart.data.MacroEol;
//import no.nordicsemi.android.uart.data.MacroIcon;
@Root
class XmlMacro {
    /**
     * Returns the command that will be sent to UART device.
     * @return the command
     */
    /**
     * Sets the command.
     * @param command the command that will be sent to UART device
     */
    @field:Text(required = false)
    var command: String? = null
    /**
     * Returns whether the icon is active.
     * @return true if it's active
     */
    /**
     * Sets whether the command is active.
     * @param active true to make it active
     */
    @field:Attribute(required = false)
    var isActive = false

    /**
     * Returns the new line type.
     * @return end of line terminator
     */
    @field:Attribute(required = false)
    var eol = MacroEol.LF
        private set

    @field:Attribute(required = false)
    private var icon = MacroIcon.LEFT

    /**
     * Sets the new line type.
     * @param eol end of line terminator
     */
    fun setEol(eol: Int) {
        this.eol = MacroEol.values()[eol]
    }
    /**
     * Returns the icon index.
     * @return the icon index
     */
    /**
     * Sets the icon index.
     * @param index index of the icon.
     */
    var iconIndex: Int
        get() = icon.index
        set(index) {
            icon = MacroIcon.values()[index]
        }

    /**
     * Returns the EOL index.
     * @return the EOL index
     */
    val eolIndex: Int
        get() = eol.index
}