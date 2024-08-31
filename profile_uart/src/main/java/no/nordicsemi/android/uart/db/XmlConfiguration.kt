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

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlIgnoreWhitespace
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("xml-configuration")
@XmlIgnoreWhitespace
data class XmlConfiguration(
	@XmlElement(false)
	var name: String = "Unnamed",

	@XmlElement(true)
	var commands: Commands = Commands()
) {

	@Serializable
	@XmlSerialName("commands")
	data class Commands(
		var commands: Array<XmlMacro> = arrayOf(
			XmlMacro(),
			XmlMacro(),
			XmlMacro(),
			XmlMacro(),
			XmlMacro(),
			XmlMacro(),
			XmlMacro(),
			XmlMacro(),
			XmlMacro()
		),

		@XmlElement(false)
		val length: Int = commands.size,
	) {
		init {
			require(commands.size == COMMANDS_COUNT) { "A Macro must have 9 commands" }
		}

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as Commands

			if (length != other.length) return false
			if (!commands.contentEquals(other.commands)) return false

			return true
		}

		override fun hashCode(): Int {
			var result = length
			result = 31 * result + commands.contentHashCode()
			return result
		}
	}

	companion object {
		const val COMMANDS_COUNT = 9
	}
}
