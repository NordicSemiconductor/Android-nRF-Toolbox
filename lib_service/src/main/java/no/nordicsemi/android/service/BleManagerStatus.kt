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

package no.nordicsemi.android.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

sealed interface BleManagerResult<T> {

    fun isRunning(): Boolean {
        return this is SuccessResult
    }

    fun hasBeenDisconnected(): Boolean {
        return this is LinkLossResult || this is DisconnectedResult || this is MissingServiceResult
    }

    fun hasBeenDisconnectedWithoutLinkLoss(): Boolean {
        return this is DisconnectedResult || this is MissingServiceResult
    }
}

sealed class DeviceHolder(val device: BluetoothDevice) {

    @SuppressLint("MissingPermission")
    fun deviceName(): String = device.name ?: device.address

}

class IdleResult<T> : BleManagerResult<T>
class ConnectingResult<T>(device: BluetoothDevice) : DeviceHolder(device), BleManagerResult<T>
class ConnectedResult<T>(device: BluetoothDevice) : DeviceHolder(device), BleManagerResult<T>
class SuccessResult<T>(device: BluetoothDevice, val data: T) : DeviceHolder(device), BleManagerResult<T>

class LinkLossResult<T>(device: BluetoothDevice, val data: T?) : DeviceHolder(device), BleManagerResult<T>
class DisconnectedResult<T>(device: BluetoothDevice) : DeviceHolder(device), BleManagerResult<T>
class UnknownErrorResult<T>(device: BluetoothDevice) : DeviceHolder(device), BleManagerResult<T>
class MissingServiceResult<T>(device: BluetoothDevice) : DeviceHolder(device), BleManagerResult<T>
