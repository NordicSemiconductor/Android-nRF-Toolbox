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

package no.nordicsemi.android.toolbox.libs.profile.data.hts

import java.util.Calendar

/**
 * HTS data class that holds the temperature data.
 *
 * @param temperature The temperature value.
 * @param unit The unit of the temperature value.
 * @param timestamp The timestamp of the measurement.
 * @param type The type of the measurement.
 */
data class HtsData(
    val temperature: Float = 0f,
    val unit: TemperatureUnitData = TemperatureUnitData.CELSIUS,
    val timestamp: Calendar? = null,
    val type: Int? = null
)

/**
 * The temperature unit data class.
 *
 * @param value The value of the temperature unit.
 */
enum class TemperatureUnitData(private val value: Int) {
    CELSIUS(0),
    FAHRENHEIT(1);

    companion object {
        fun create(value: Int): TemperatureUnitData? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

/**
 * HTS service data class that holds the HTS data.
 *
 * @param data The HTS data.
 * @param temperatureUnit The temperature unit.
 */
data class HTSServiceData(
    val data: HtsData = HtsData(),
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
)