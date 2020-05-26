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
package no.nordicsemi.android.nrftoolbox.hr;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.ble.common.callback.hr.BodySensorLocationDataCallback;
import no.nordicsemi.android.ble.common.callback.hr.HeartRateMeasurementDataCallback;
import no.nordicsemi.android.ble.common.profile.hr.BodySensorLocation;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.battery.BatteryManager;
import no.nordicsemi.android.nrftoolbox.parser.BodySensorLocationParser;
import no.nordicsemi.android.nrftoolbox.parser.HeartRateMeasurementParser;

/**
 * HRSManager class performs BluetoothGatt operations for connection, service discovery,
 * enabling notification and reading characteristics.
 * All operations required to connect to device with BLE Heart Rate Service and reading
 * heart rate values are performed here.
 */
public class HRManager extends BatteryManager<HRManagerCallbacks> {
	static final UUID HR_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
	private static final UUID BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");
	private static final UUID HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic heartRateCharacteristic, bodySensorLocationCharacteristic;

	private static HRManager managerInstance = null;

	/**
	 * Singleton implementation of HRSManager class.
	 */
	public static synchronized HRManager getInstance(final Context context) {
		if (managerInstance == null) {
			managerInstance = new HRManager(context);
		}
		return managerInstance;
	}

	private HRManager(final Context context) {
		super(context);
	}

	@NonNull
	@Override
	protected BatteryManagerGattCallback getGattCallback() {
		return new HeartRateManagerCallback();
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery,
	 * receiving notification, etc.
	 */
	private final class HeartRateManagerCallback extends BatteryManagerGattCallback {

		@Override
		protected void initialize() {
			super.initialize();
			readCharacteristic(bodySensorLocationCharacteristic)
					.with(new BodySensorLocationDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + BodySensorLocationParser.parse(data) + "\" received");
							super.onDataReceived(device, data);
						}

						@Override
						public void onBodySensorLocationReceived(@NonNull final BluetoothDevice device,
																 @BodySensorLocation final int sensorLocation) {
							mCallbacks.onBodySensorLocationReceived(device, sensorLocation);
						}
					})
					.fail((device, status) -> log(Log.WARN, "Body Sensor Location characteristic not found"))
					.enqueue();
			setNotificationCallback(heartRateCharacteristic)
					.with(new HeartRateMeasurementDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + HeartRateMeasurementParser.parse(data) + "\" received");
							super.onDataReceived(device, data);
						}

						@Override
						public void onHeartRateMeasurementReceived(@NonNull final BluetoothDevice device,
																   @IntRange(from = 0) final int heartRate,
																   @Nullable final Boolean contactDetected,
																   @Nullable @IntRange(from = 0) final Integer energyExpanded,
																   @Nullable final List<Integer> rrIntervals) {
							mCallbacks.onHeartRateMeasurementReceived(device, heartRate, contactDetected, energyExpanded, rrIntervals);
						}
					});
			enableNotifications(heartRateCharacteristic).enqueue();
		}

		@Override
		protected boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(HR_SERVICE_UUID);
			if (service != null) {
				heartRateCharacteristic = service.getCharacteristic(HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID);
			}
			return heartRateCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
			super.isOptionalServiceSupported(gatt);
			final BluetoothGattService service = gatt.getService(HR_SERVICE_UUID);
			if (service != null) {
				bodySensorLocationCharacteristic = service.getCharacteristic(BODY_SENSOR_LOCATION_CHARACTERISTIC_UUID);
			}
			return bodySensorLocationCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			super.onDeviceDisconnected();
			bodySensorLocationCharacteristic = null;
			heartRateCharacteristic = null;
		}
	}
}
