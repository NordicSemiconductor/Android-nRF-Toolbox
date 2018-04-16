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
import android.support.annotation.NonNull;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.ReadRequest;
import no.nordicsemi.android.ble.Request;
import no.nordicsemi.android.ble.callback.Data;
import no.nordicsemi.android.ble.callback.profile.BatteryLevelCallback;
import no.nordicsemi.android.ble.callback.profile.CyclingSpeedAndCadenceCallback;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.csc.settings.SettingsFragment;
import no.nordicsemi.android.nrftoolbox.parser.CSCMeasurementParser;

public class CSCManager extends BleManager<CSCManagerCallbacks> {
	/**
	 * Cycling Speed and Cadence service UUID.
	 */
	public final static UUID CYCLING_SPEED_AND_CADENCE_SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb");
	/**
	 * Cycling Speed and Cadence Measurement characteristic UUID.
	 */
	private static final UUID CSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb");
	/**
	 * Battery Service UUID.
	 */
	private final static UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
	/**
	 * Battery Level characteristic UUID.
	 */
	private final static UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

	private final SharedPreferences preferences;
	private BluetoothGattCharacteristic mCSCMeasurementCharacteristic;
	private BluetoothGattCharacteristic mBatteryLevelCharacteristic;

	CSCManager(final Context context) {
		super(context);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	protected BleManagerGattCallback getGattCallback() {
		return mGattCallback;
	}

	public void readBatteryLevelCharacteristic() {
		readCharacteristic(mBatteryLevelCharacteristic)
				.with(new BatteryLevelCallback() {
					@Override
					public void onBatteryValueChanged(final int batteryLevel) {
						mCallbacks.onBatteryLevelChanged(getBluetoothDevice(), batteryLevel);
					}

					@Override
					public void onInvalidDataReceived(final @NonNull Data data) {
						log(LogContract.Log.Level.WARNING, "Invalid Battery Level data received: " + data);
					}
				})
				.fail(status -> log(LogContract.Log.Level.WARNING, "Battery Level characteristic not found"));
	}

	public void enableBatteryLevelCharacteristicNotifications() {
		// If the Battery Level characteristic is null, the request will be ignored
		enableNotifications(mBatteryLevelCharacteristic)
				.with(new BatteryLevelCallback() {
					@Override
					public void onBatteryValueChanged(final int batteryLevel) {
						mCallbacks.onBatteryLevelChanged(getBluetoothDevice(), batteryLevel);
					}

					@Override
					public void onInvalidDataReceived(final @NonNull Data data) {
						log(LogContract.Log.Level.WARNING, "Invalid Battery Level data received: " + data);
					}
				})
				.done(() -> log(LogContract.Log.Level.INFO, "Battery Level notifications enabled"))
				.fail(status -> log(LogContract.Log.Level.WARNING, "Battery Level characteristic not found"));
	}

	public void disableBatteryLevelCharacteristicNotifications() {
		disableNotifications(mBatteryLevelCharacteristic)
				.done(() -> log(LogContract.Log.Level.INFO, "Battery Level notifications disabled"));
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
	 */
	private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

		@Override
		protected void initialize(@NonNull final BluetoothDevice device) {
			enableBatteryLevelCharacteristicNotifications();

			// CSC characteristic is required
			enableNotifications(mCSCMeasurementCharacteristic)
					.with(new CyclingSpeedAndCadenceCallback() {
						@Override
						public void onDataReceived(final @NonNull Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + CSCMeasurementParser.parse(data) + "\" received");

							// Pass through received data
							super.onDataReceived(data);
						}

						@Override
						protected float getWheelCircumference() {
							return Integer.parseInt(preferences.getString(SettingsFragment.SETTINGS_WHEEL_SIZE, String.valueOf(SettingsFragment.SETTINGS_WHEEL_SIZE_DEFAULT)));
						}

						@Override
						protected void onDistanceChanged(final float totalDistance, final float distance, final float speed) {
							mCallbacks.onDistanceChanged(device, totalDistance, distance, speed);
						}

						@Override
						protected void onCrankDataChanged(final float crankCadence, final float gearRatio) {
							mCallbacks.onCrankDataChanged(device, crankCadence, gearRatio);
						}

						@Override
						public void onInvalidDataReceived(final @NonNull Data data) {
							log(LogContract.Log.Level.WARNING, "Invalid CSC Measurement data received: " + data);
						}
					});
		}

		@Override
		public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(CYCLING_SPEED_AND_CADENCE_SERVICE_UUID);
			if (service != null) {
				mCSCMeasurementCharacteristic = service.getCharacteristic(CSC_MEASUREMENT_CHARACTERISTIC_UUID);
			}
			return mCSCMeasurementCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(BATTERY_SERVICE_UUID);
			if (service != null) {
				mBatteryLevelCharacteristic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID);
			}
			return mBatteryLevelCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			mCSCMeasurementCharacteristic = null;
			mBatteryLevelCharacteristic = null;
		}
	};
}
