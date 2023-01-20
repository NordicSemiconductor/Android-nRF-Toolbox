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

package no.nordicsemi.android.hts.view

import no.nordicsemi.android.common.theme.view.RadioButtonItem
import no.nordicsemi.android.common.theme.view.RadioGroupViewEntity

private const val DISPLAY_FAHRENHEIT = "°F"
private const val DISPLAY_CELSIUS = "°C"
private const val DISPLAY_KELVIN = "°K"

internal fun displayTemperature(value: Float, temperatureUnit: TemperatureUnit): String {
    return when (temperatureUnit) {
        TemperatureUnit.CELSIUS -> String.format("%.1f °C", value)
        TemperatureUnit.FAHRENHEIT -> String.format("%.1f °F", value * 1.8f + 32f)
        TemperatureUnit.KELVIN -> String.format("%.1f °K", value + 273.15f)
    }
}

internal fun String.toTemperatureUnit(): TemperatureUnit {
    return when (this) {
        DISPLAY_CELSIUS -> TemperatureUnit.CELSIUS
        DISPLAY_FAHRENHEIT -> TemperatureUnit.FAHRENHEIT
        DISPLAY_KELVIN -> TemperatureUnit.KELVIN
        else -> throw IllegalArgumentException("Can't create TemperatureUnit from this label: $this")
    }
}

internal fun TemperatureUnit.temperatureSettingsItems(): RadioGroupViewEntity {
    return RadioGroupViewEntity(
        TemperatureUnit.values().map { createRadioButtonItem(it, this) }
    )
}

private fun createRadioButtonItem(unit: TemperatureUnit, selectedTemperatureUnit: TemperatureUnit): RadioButtonItem {
    return RadioButtonItem(displayTemperature(unit), unit == selectedTemperatureUnit)
}

private fun displayTemperature(unit: TemperatureUnit): String {
    return when (unit) {
        TemperatureUnit.CELSIUS -> DISPLAY_CELSIUS
        TemperatureUnit.FAHRENHEIT -> DISPLAY_FAHRENHEIT
        TemperatureUnit.KELVIN -> DISPLAY_KELVIN
    }
}
