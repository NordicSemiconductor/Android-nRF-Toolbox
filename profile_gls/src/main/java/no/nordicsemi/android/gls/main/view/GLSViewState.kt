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

import no.nordicsemi.android.gls.data.GLSServiceData
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.profile.gls.data.GLSMeasurementContext
import no.nordicsemi.android.kotlin.ble.profile.gls.data.GLSRecord
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RequestStatus

internal data class GLSViewState(
    val glsServiceData: GLSServiceData = GLSServiceData(),
    val deviceName: String? = null,
    val missingServices: Boolean = false
) {

    val disconnectStatus = if (missingServices) {
        BleGattConnectionStatus.NOT_SUPPORTED
    } else {
        glsServiceData.connectionState?.status ?: BleGattConnectionStatus.UNKNOWN
    }

    fun copyWithNewConnectionState(connectionState: GattConnectionStateWithStatus): GLSViewState {
        return copy(glsServiceData = glsServiceData.copy(connectionState = connectionState))
    }

    fun copyAndClear(): GLSViewState {
        return copy(glsServiceData = glsServiceData.copy(records = mapOf(), requestStatus = RequestStatus.IDLE))
    }

    fun copyWithNewRequestStatus(requestStatus: RequestStatus): GLSViewState {
        return copy(glsServiceData = glsServiceData.copy(requestStatus = requestStatus))
    }

    fun copyWithNewBatteryLevel(batteryLevel: Int): GLSViewState {
        return copy(glsServiceData = glsServiceData.copy(batteryLevel = batteryLevel))
    }

    //todo optimise
    fun copyWithNewRecord(record: GLSRecord): GLSViewState {
        val records = glsServiceData.records.toMutableMap()
        records[record] = null
        return copy(glsServiceData = glsServiceData.copy(records = records.toMap()))
    }

    //todo optimise
    fun copyWithNewContext(context: GLSMeasurementContext): GLSViewState {
        val records = glsServiceData.records.toMutableMap()
        return records.keys.firstOrNull { it.sequenceNumber == context.sequenceNumber }?.let {
            records[it] = context
            copy(glsServiceData = glsServiceData.copy(records = records.toMap()))
        } ?: this
    }
}
