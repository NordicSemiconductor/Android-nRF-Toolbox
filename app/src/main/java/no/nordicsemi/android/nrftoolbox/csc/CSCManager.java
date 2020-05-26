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

package no.nordicsemi.android.nrftoolbox.csc;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.UUID;

import no.nordicsemi.android.ble.common.callback.csc.CyclingSpeedAndCadenceMeasurementDataCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.battery.BatteryManager;
import no.nordicsemi.android.nrftoolbox.csc.settings.SettingsFragment;
import no.nordicsemi.android.nrftoolbox.parser.CSCMeasurementParser;

public class CSCManager extends BatteryManager<CSCManagerCallbacks> {
	/** Cycling Speed and Cadence service UUID. */
	final static UUID CYCLING_SPEED_AND_CADENCE_SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb");
	/** Cycling Speed and Cadence Measurement characteristic UUID. */
	private final static UUID CSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb");

	private final SharedPreferences preferences;
	private BluetoothGattCharacteristic cscMeasurementCharacteristic;

	CSCManager(final Context context) {
		super(context);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@NonNull
	@Override
	protected BatteryManagerGattCallback getGattCallback() {
		return new CSCManagerGattCallback();
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery,
	 * receiving indication, etc.
	 */
	private class CSCManagerGattCallback extends BatteryManagerGattCallback {

		@Override
		protected void initialize() {
			super.initialize();

			// CSC characteristic is required
			setNotificationCallback(cscMeasurementCharacteristic)
					.with(new CyclingSpeedAndCadenceMeasurementDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, final @NonNull Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + CSCMeasurementParser.parse(data) + "\" received");

							// Pass through received data
							super.onDataReceived(device, data);
						}

						@Override
						public float getWheelCircumference() {
							return Integer.parseInt(preferences.getString(SettingsFragment.SETTINGS_WHEEL_SIZE,
                                    String.valueOf(SettingsFragment.SETTINGS_WHEEL_SIZE_DEFAULT)));
						}

						@Override
						public void onDistanceChanged(@NonNull final BluetoothDevice device,
													  @FloatRange(from = 0) final float totalDistance,
													  @FloatRange(from = 0) final float distance,
													  @FloatRange(from = 0) final float speed) {
							mCallbacks.onDistanceChanged(device, totalDistance, distance, speed);
						}

						@Override
						public void onCrankDataChanged(@NonNull final BluetoothDevice device,
													   @FloatRange(from = 0) final float crankCadence,
													   final float gearRatio) {
							mCallbacks.onCrankDataChanged(device, crankCadence, gearRatio);
						}

						@Override
						public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                                          @NonNull final Data data) {
							log(Log.WARN, "Invalid CSC Measurement data received: " + data);
						}
					});
			enableNotifications(cscMeasurementCharacteristic).enqueue();
		}

		@Override
		public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(CYCLING_SPEED_AND_CADENCE_SERVICE_UUID);
			if (service != null) {
				cscMeasurementCharacteristic = service.getCharacteristic(CSC_MEASUREMENT_CHARACTERISTIC_UUID);
			}
			return cscMeasurementCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			super.onDeviceDisconnected();
			cscMeasurementCharacteristic = null;
		}
	}
}
