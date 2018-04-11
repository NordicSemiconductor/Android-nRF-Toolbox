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
import no.nordicsemi.android.nrftoolbox.parser.CSCMeasurementParser;

public class CSCManager extends BleManager<CSCManagerCallbacks> {
	/** Cycling Speed and Cadence service UUID */
	public final static UUID CYCLING_SPEED_AND_CADENCE_SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb");
	/** Cycling Speed and Cadence Measurement characteristic UUID */
	private static final UUID CSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb");

	private static final byte WHEEL_REVOLUTIONS_DATA_PRESENT = 0x01; // 1 bit
	private static final byte CRANK_REVOLUTION_DATA_PRESENT = 0x02; // 1 bit

	private BluetoothGattCharacteristic mCSCMeasurementCharacteristic;

	public CSCManager(final Context context) {
		super(context);
	}

	@Override
	protected BleManagerGattCallback getGattCallback() {
		return mGattCallback;
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
	 */
	private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

		@Override
		protected Deque<Request> initGatt(final BluetoothGatt gatt) {
			final LinkedList<Request> requests = new LinkedList<>();
			requests.add(Request.newEnableNotificationsRequest(mCSCMeasurementCharacteristic));
			return requests;
		}

		@Override
		public boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(CYCLING_SPEED_AND_CADENCE_SERVICE_UUID);
			if (service != null) {
				mCSCMeasurementCharacteristic = service.getCharacteristic(CSC_MEASUREMENT_CHARACTERISTIC_UUID);
			}
			return mCSCMeasurementCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			mCSCMeasurementCharacteristic = null;
		}

		@Override
		public void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			Logger.a(mLogSession, "\"" + CSCMeasurementParser.parse(characteristic) + "\" received");

			// Decode the new data
			int offset = 0;
			final int flags = characteristic.getValue()[offset]; // 1 byte
			offset += 1;

			final boolean wheelRevPresent = (flags & WHEEL_REVOLUTIONS_DATA_PRESENT) > 0;
			final boolean crankRevPreset = (flags & CRANK_REVOLUTION_DATA_PRESENT) > 0;

			if (wheelRevPresent) {
				final int wheelRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset);
				offset += 4;

				final int lastWheelEventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset); // 1/1024 s
				offset += 2;

				// Notify listener about the new measurement
				mCallbacks.onWheelMeasurementReceived(gatt.getDevice(), wheelRevolutions, lastWheelEventTime);
			}

			if (crankRevPreset) {
				final int crankRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
				offset += 2;

				final int lastCrankEventTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
				// offset += 2;

				// Notify listener about the new measurement
				mCallbacks.onCrankMeasurementReceived(gatt.getDevice(), crankRevolutions, lastCrankEventTime);
			}
		}
	};
}
