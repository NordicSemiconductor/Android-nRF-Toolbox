/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.nrftoolbox.proximity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.BleServerManager;
import no.nordicsemi.android.ble.common.data.alert.AlertLevelData;

class ProximityServerManager extends BleServerManager {

	ProximityServerManager(@NonNull final Context context) {
		super(context);
	}

	@Override
	public void log(final int priority, @NonNull final String message) {
		Log.println(priority, "BleManager", message);
	}

	@NonNull
	@Override
	protected List<BluetoothGattService> initializeServer() {
		final List<BluetoothGattService> services = new ArrayList<>();
		services.add(
				service(ProximityManager.IMMEDIATE_ALERT_SERVICE_UUID,
						characteristic(ProximityManager.ALERT_LEVEL_CHARACTERISTIC_UUID,
								BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
								BluetoothGattCharacteristic.PERMISSION_WRITE))
		);
		services.add(
				service(ProximityManager.LINK_LOSS_SERVICE_UUID,
						characteristic(ProximityManager.ALERT_LEVEL_CHARACTERISTIC_UUID,
								BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ,
								BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ,
								AlertLevelData.highAlert()))
		);
		return services;
	}
}
