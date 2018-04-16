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
package no.nordicsemi.android.nrftoolbox.bpm;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.Data;
import no.nordicsemi.android.ble.callback.profile.BatteryLevelDataCallback;
import no.nordicsemi.android.ble.callback.profile.BloodPressureMeasurementDataCallback;
import no.nordicsemi.android.ble.callback.profile.IntermediateCuffPressureDataCallback;
import no.nordicsemi.android.ble.profile.BloodPressureMeasurementCallback;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.parser.BloodPressureMeasurementParser;
import no.nordicsemi.android.nrftoolbox.parser.IntermediateCuffPressureParser;

@SuppressWarnings({"unused", "WeakerAccess"})
public class BPMManager extends BleManager<BPMManagerCallbacks> {
	/** Blood Pressure service UUID. */
	public final static UUID BP_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
	/** Blood Pressure Measurement characteristic UUID. */
	private static final UUID BPM_CHARACTERISTIC_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb");
	/** Intermediate Cuff Pressure characteristic UUID. */
	private static final UUID ICP_CHARACTERISTIC_UUID = UUID.fromString("00002A36-0000-1000-8000-00805f9b34fb");
	/** Battery Service UUID. */
	private final static UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
	/** Battery Level characteristic UUID. */
	private final static UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic mBPMCharacteristic, mICPCharacteristic;
	private BluetoothGattCharacteristic mBatteryLevelCharacteristic;

	private static BPMManager managerInstance = null;

	/**
	 * Returns the singleton implementation of BPMManager
	 */
	public static synchronized BPMManager getBPMManager(final Context context) {
		if (managerInstance == null) {
			managerInstance = new BPMManager(context);
		}
		return managerInstance;
	}

	private BPMManager(final Context context) {
		super(context);
	}

	@Override
	protected BleManagerGattCallback getGattCallback() {
		return mGattCallback;
	}

	public void readBatteryLevelCharacteristic() {
		readCharacteristic(mBatteryLevelCharacteristic)
				.with(new BatteryLevelDataCallback() {
					@Override
					public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
						mCallbacks.onBatteryLevelChanged(device, batteryLevel);
					}

					@Override
					public void onInvalidDataReceived(@NonNull final BluetoothDevice device, final @NonNull Data data) {
						log(LogContract.Log.Level.WARNING, "Invalid Battery Level data received: " + data);
					}
				})
				.fail(status -> log(LogContract.Log.Level.WARNING, "Battery Level characteristic not found"));
	}

	public void enableBatteryLevelCharacteristicNotifications() {
		// If the Battery Level characteristic is null, the request will be ignored
		enableNotifications(mBatteryLevelCharacteristic)
				.with(new BatteryLevelDataCallback() {
					@Override
					public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
						mCallbacks.onBatteryLevelChanged(device, batteryLevel);
					}

					@Override
					public void onInvalidDataReceived(@NonNull final BluetoothDevice device, final @NonNull Data data) {
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
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving notification, etc
	 */
	private final BleManagerGattCallback  mGattCallback = new BleManagerGattCallback() {

		@Override
		protected void initialize(@NonNull final BluetoothDevice device) {
			readBatteryLevelCharacteristic();
			enableBatteryLevelCharacteristicNotifications();
			enableNotifications(mICPCharacteristic)
					.with(new IntermediateCuffPressureDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + IntermediateCuffPressureParser.parse(data) + "\" received");

							// Pass through received data
							super.onDataReceived(device, data);
						}

						@Override
						public void onIntermediateCuffPressureReceived(@NonNull final BluetoothDevice device, final float cuffPressure, final int unit, @Nullable final Float pulseRate, @Nullable final Integer userID, @Nullable final Status status, @Nullable final Calendar calendar) {
							mCallbacks.onIntermediateCuffPressureReceived(device, cuffPressure, unit, pulseRate, userID, status, calendar);
						}

						@Override
						public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.WARNING, "Invalid ICP data received: " + data);
						}
					});
			enableIndications(mBPMCharacteristic)
					.with(new BloodPressureMeasurementDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + BloodPressureMeasurementParser.parse(data) + "\" received");

							// Pass through received data
							super.onDataReceived(device, data);
						}

						@Override
						public void onBloodPressureMeasurementReceived(@NonNull final BluetoothDevice device, final float systolic, final float diastolic, final float meanArterialPressure, final int unit, @Nullable final Float pulseRate, @Nullable final Integer userID, @Nullable final Status status, @Nullable final Calendar calendar) {
							mCallbacks.onBloodPressureMeasurementReceived(device, systolic, diastolic, meanArterialPressure, unit, pulseRate, userID, status, calendar);
						}

						@Override
						public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.WARNING, "Invalid BPM data received: " + data);
						}
					});
		}

		@Override
		protected boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(BP_SERVICE_UUID);
			if (service != null) {
				mBPMCharacteristic = service.getCharacteristic(BPM_CHARACTERISTIC_UUID);
				mICPCharacteristic = service.getCharacteristic(ICP_CHARACTERISTIC_UUID);
			}
			return mBPMCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(BATTERY_SERVICE_UUID);
			if (service != null) {
				mBatteryLevelCharacteristic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID);
			}
			return mICPCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			mICPCharacteristic = null;
			mBPMCharacteristic = null;
			mBatteryLevelCharacteristic = null;
		}
	};
}
