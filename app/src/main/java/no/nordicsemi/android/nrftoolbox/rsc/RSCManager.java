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

package no.nordicsemi.android.nrftoolbox.rsc;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

import no.nordicsemi.android.ble.common.callback.rsc.RunningSpeedAndCadenceMeasurementDataCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.battery.BatteryManager;
import no.nordicsemi.android.nrftoolbox.parser.RSCMeasurementParser;

public class RSCManager extends BatteryManager<RSCManagerCallbacks> {
	/** Running Speed and Cadence Measurement service UUID */
	static final UUID RUNNING_SPEED_AND_CADENCE_SERVICE_UUID = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
	/** Running Speed and Cadence Measurement characteristic UUID */
	private static final UUID RSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A53-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic rscMeasurementCharacteristic;

	RSCManager(final Context context) {
		super(context);
	}

	@NonNull
	@Override
	protected BatteryManagerGattCallback getGattCallback() {
		return new RSCManagerGattCallback();
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery,
	 * receiving indication, etc.
	 */
	private class RSCManagerGattCallback extends BatteryManagerGattCallback {

		@Override
		protected void initialize() {
			super.initialize();
			setNotificationCallback(rscMeasurementCharacteristic)
					.with(new RunningSpeedAndCadenceMeasurementDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + RSCMeasurementParser.parse(data) + "\" received");
							super.onDataReceived(device, data);
						}

						@Override
						public void onRSCMeasurementReceived(@NonNull final BluetoothDevice device, final boolean running,
															 final float instantaneousSpeed, final int instantaneousCadence,
															 @Nullable final Integer strideLength,
															 @Nullable final Long totalDistance) {
							mCallbacks.onRSCMeasurementReceived(device, running, instantaneousSpeed,
									instantaneousCadence, strideLength, totalDistance);
						}
					});
			enableNotifications(rscMeasurementCharacteristic).enqueue();
		}

		@Override
		public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(RUNNING_SPEED_AND_CADENCE_SERVICE_UUID);
			if (service != null) {
				rscMeasurementCharacteristic = service.getCharacteristic(RSC_MEASUREMENT_CHARACTERISTIC_UUID);
			}
			return rscMeasurementCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			super.onDeviceDisconnected();
			rscMeasurementCharacteristic = null;
		}
	}
}
