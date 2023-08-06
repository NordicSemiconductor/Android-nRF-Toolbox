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

package no.nordicsemi.android.gls.main.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.gls.R
import no.nordicsemi.android.gls.data.WorkingMode
import no.nordicsemi.android.kotlin.ble.profile.gls.data.ConcentrationUnit
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RecordType

@Composable
internal fun RecordType?.toDisplayString(): String {
    return when (this) {
        RecordType.CAPILLARY_WHOLE_BLOOD -> stringResource(id = R.string.gls_type_capillary_whole_blood)
        RecordType.CAPILLARY_PLASMA -> stringResource(id = R.string.gls_type_capillary_plasma)
        RecordType.VENOUS_WHOLE_BLOOD -> stringResource(id = R.string.gls_type_venous_whole_blood)
        RecordType.VENOUS_PLASMA -> stringResource(id = R.string.gls_type_venous_plasma)
        RecordType.ARTERIAL_WHOLE_BLOOD -> stringResource(id = R.string.gls_type_arterial_whole_blood)
        RecordType.ARTERIAL_PLASMA -> stringResource(id = R.string.gls_type_arterial_plasma)
        RecordType.UNDETERMINED_WHOLE_BLOOD -> stringResource(id = R.string.gls_type_undetermined_whole_blood)
        RecordType.UNDETERMINED_PLASMA -> stringResource(id = R.string.gls_type_undetermined_plasma)
        RecordType.INTERSTITIAL_FLUID -> stringResource(id = R.string.gls_type_interstitial_fluid)
        RecordType.CONTROL_SOLUTION -> stringResource(id = R.string.gls_type_control_solution)
        null -> stringResource(id = R.string.gls_type_reserved)
    }
}

@Composable
internal fun ConcentrationUnit.toDisplayString(): String {
    return when (this) {
        //TODO("Check unit_kgpl --> mgpdl")
        ConcentrationUnit.UNIT_KGPL -> stringResource(id = R.string.gls_unit_mgpdl)
        ConcentrationUnit.UNIT_MOLPL -> stringResource(id = R.string.gls_unit_mmolpl)
    }
}

@Composable
internal fun WorkingMode.toDisplayString(): String {
    return when (this) {
        WorkingMode.ALL -> stringResource(id = R.string.gls__working_mode__all)
        WorkingMode.LAST -> stringResource(id = R.string.gls__working_mode__last)
        WorkingMode.FIRST -> stringResource(id = R.string.gls__working_mode__first)
    }
}

@Composable
internal fun glucoseConcentrationDisplayValue(value: Float, unit: ConcentrationUnit): String {
    val result = when (unit) {
        ConcentrationUnit.UNIT_KGPL -> value * 100000.0f
        ConcentrationUnit.UNIT_MOLPL -> value * 1000.0f
    }
    return String.format("%.2f %s", result, unit.toDisplayString())
}
