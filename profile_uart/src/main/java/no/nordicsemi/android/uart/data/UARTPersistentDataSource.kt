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

package no.nordicsemi.android.uart.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XML
import no.nordicsemi.android.uart.db.Configuration
import no.nordicsemi.android.uart.db.ConfigurationsDao
import no.nordicsemi.android.uart.db.XmlConfiguration
import no.nordicsemi.android.uart.db.XmlMacro
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UARTPersistentDataSource @Inject constructor(
    private val configurationsDao: ConfigurationsDao,
) {
    private val serializer = XML {
        recommended()
    }

    fun getConfigurations(): Flow<List<UARTConfiguration>> = configurationsDao.load()
        .map { list ->
            list.mapNotNull { it.toDomain() }
        }

    private fun Configuration.toDomain(): UARTConfiguration? {
        return try {
            val xml: String = xml
            val configuration = serializer.decodeFromString<XmlConfiguration>(xml)

            UARTConfiguration(
                _id,
                configuration.name,
                createMacro(configuration.commands.commands)
            )
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    private fun createMacro(macros: Array<XmlMacro>): List<UARTMacro?> {
        return macros.map {
            if (it.command == null) return@map null
            val icon = MacroIcon.create(it.iconIndex)
            UARTMacro(icon, it.command, it.eol)
        }
    }

    suspend fun saveConfiguration(configuration: UARTConfiguration) {
        val xml = serializer.encodeToString(configuration.toXmlConfiguration())

        configurationsDao.insert(Configuration(configuration.id, configuration.name, xml, 0))
    }

    suspend fun deleteConfiguration(configuration: UARTConfiguration) {
        configurationsDao.delete(configuration.name)
    }

    private fun UARTConfiguration.toXmlConfiguration(): XmlConfiguration {
        val xmlConfiguration = XmlConfiguration()
        xmlConfiguration.name = name
        val commands = macros.map { macro ->
            macro?.let {
                XmlMacro().apply {
                    eolIndex = it.newLineChar.index
                    command = it.command
                    iconIndex = it.icon.index
                }
            } ?: XmlMacro()
        }.toTypedArray()
        xmlConfiguration.commands = XmlConfiguration.Commands(commands = commands)
        return xmlConfiguration
    }
}
