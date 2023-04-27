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

package no.nordicsemi.android.csc.view

import no.nordicsemi.android.common.theme.view.RadioButtonItem
import no.nordicsemi.android.common.theme.view.RadioGroupViewEntity
import no.nordicsemi.android.csc.data.SpeedUnit
import no.nordicsemi.android.kotlin.ble.profile.csc.data.CSCData
import java.util.Locale

private const val DISPLAY_M_S = "m/s"
private const val DISPLAY_KM_H = "km/h"
private const val DISPLAY_MPH = "mph"

internal fun CSCData.speedWithSpeedUnit(speedUnit: SpeedUnit): Float {
    return when (speedUnit) {
        SpeedUnit.M_S -> speed
        SpeedUnit.KM_H -> speed * 3.6f
        SpeedUnit.MPH -> speed * 2.2369f
    }
}

internal fun CSCData.displaySpeed(speedUnit: SpeedUnit): String {
    val speedWithUnit = speedWithSpeedUnit(speedUnit)
    return when (speedUnit) {
        SpeedUnit.M_S -> String.format("%.1f m/s", speedWithUnit)
        SpeedUnit.KM_H -> String.format("%.1f km/h", speedWithUnit)
        SpeedUnit.MPH -> String.format("%.1f mph", speedWithUnit)
    }
}

internal fun CSCData.displayCadence(): String {
    return String.format("%.0f RPM", cadence)
}

internal fun CSCData.displayDistance(speedUnit: SpeedUnit): String {
    return when (speedUnit) {
        SpeedUnit.M_S -> String.format("%.0f m", distance)
        SpeedUnit.KM_H -> String.format("%.0f m", distance)
        SpeedUnit.MPH -> String.format("%.0f yd", distance.toYards())
    }
}

internal fun CSCData.displayTotalDistance(speedUnit: SpeedUnit): String {
    return when (speedUnit) {
        SpeedUnit.M_S -> String.format("%.2f m", totalDistance)
        SpeedUnit.KM_H -> String.format("%.2f km", totalDistance.toKilometers())
        SpeedUnit.MPH -> String.format("%.2f mile", totalDistance.toMiles())
    }
}

internal fun CSCData.displayGearRatio(): String {
    return String.format(Locale.US, "%.1f", gearRatio)
}

internal fun String.toSpeedUnit(): SpeedUnit {
    return when (this) {
        DISPLAY_KM_H -> SpeedUnit.KM_H
        DISPLAY_M_S -> SpeedUnit.M_S
        DISPLAY_MPH -> SpeedUnit.MPH
        else -> throw IllegalArgumentException("Can't create SpeedUnit from this label: $this")
    }
}

internal fun SpeedUnit.temperatureSettingsItems(): RadioGroupViewEntity {
    return RadioGroupViewEntity(
        SpeedUnit.values().map { createRadioButtonItem(it, this) }
    )
}

private fun createRadioButtonItem(unit: SpeedUnit, selectedSpeedUnit: SpeedUnit): RadioButtonItem {
    return RadioButtonItem(displayTemperature(unit), unit == selectedSpeedUnit)
}

private fun displayTemperature(unit: SpeedUnit): String {
    return when (unit) {
        SpeedUnit.KM_H -> DISPLAY_KM_H
        SpeedUnit.M_S -> DISPLAY_M_S
        SpeedUnit.MPH -> DISPLAY_MPH
    }
}

private fun Float.toYards(): Float {
    return this*1.0936f
}

private fun Float.toKilometers(): Float {
    return this/1000f
}

private fun Float.toMiles(): Float {
    return this*0.0006f
}

