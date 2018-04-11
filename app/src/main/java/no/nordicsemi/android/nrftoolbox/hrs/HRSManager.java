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
package no.nordicsemi.android.nrftoolbox.hrs;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.Request;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.parser.BodySensorLocationParser;
import no.nordicsemi.android.nrftoolbox.parser.HeartRateMeasurementParser;

/**
 * HRSManager class performs BluetoothGatt operations for connection, service discovery, enabling notification and reading characteristics. All operations required to connect to device with BLE HR
 * Service and reading heart rate values are performed here. HRSActivity implements HRSManagerCallbacks in order to receive callbacks of BluetoothGatt operations
 */
public class HRSManager extends BleManager<HRSManagerCallbacks> {
	public final static UUID HR_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
	private static final UUID HR_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");
	private static final UUID HR_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic mHRCharacteristic, mHRLocationCharacteristic;

	private static HRSManager managerInstance = null;

	/**
	 * singleton implementation of HRSManager class
	 */
	public static synchronized HRSManager getInstance(final Context context) {
		if (managerInstance == null) {
			managerInstance = new HRSManager(context);
		}
		return managerInstance;
	}

	public HRSManager(final Context context) {
		super(context);
	}

	@Override
	protected BleManagerGattCallback getGattCallback() {
		return mGattCallback;
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving notification, etc
	 */
	private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

		@Override
		protected Deque<Request> initGatt(final BluetoothGatt gatt) {
			final LinkedList<Request> requests = new LinkedList<>();
			if (mHRLocationCharacteristic != null)
				requests.add(Request.newReadRequest(mHRLocationCharacteristic));
			requests.add(Request.newEnableNotificationsRequest(mHRCharacteristic));
			return requests;
		}

		@Override
		protected boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(HR_SERVICE_UUID);
			if (service != null) {
				mHRCharacteristic = service.getCharacteristic(HR_CHARACTERISTIC_UUID);
			}
			return mHRCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(HR_SERVICE_UUID);
			if (service != null) {
				mHRLocationCharacteristic = service.getCharacteristic(HR_SENSOR_LOCATION_CHARACTERISTIC_UUID);
			}
			return mHRLocationCharacteristic != null;
		}

		@Override
		public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			Logger.a(mLogSession, "\"" + BodySensorLocationParser.parse(characteristic) + "\" received");

			final String sensorPosition = getBodySensorPosition(characteristic.getValue()[0]);
			//This will send callback to HRSActivity when HR sensor position on body is found in HR device
			mCallbacks.onHRSensorPositionFound(gatt.getDevice(), sensorPosition);
		}

		@Override
		protected void onDeviceDisconnected() {
			mHRLocationCharacteristic = null;
			mHRCharacteristic = null;
		}

		@Override
		public void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			Logger.a(mLogSession, "\"" + HeartRateMeasurementParser.parse(characteristic) + "\" received");

			int hrValue;
			if (isHeartRateInUINT16(characteristic.getValue()[0])) {
				hrValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1);
			} else {
				hrValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
			}
			//This will send callback to HRSActivity when new HR value is received from HR device
			mCallbacks.onHRValueReceived(gatt.getDevice(), hrValue);
		}
	};

	/**
	 * This method will decode and return Heart rate sensor position on body
	 */
	private String getBodySensorPosition(final byte bodySensorPositionValue) {
		final String[] locations = getContext().getResources().getStringArray(R.array.hrs_locations);
		if (bodySensorPositionValue > locations.length)
			return getContext().getString(R.string.hrs_location_other);
		return locations[bodySensorPositionValue];
	}

	/**
	 * This method will check if Heart rate value is in 8 bits or 16 bits
	 */
	private boolean isHeartRateInUINT16(final byte value) {
		return ((value & 0x01) != 0);
	}

}
