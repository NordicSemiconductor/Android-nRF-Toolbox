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
import no.nordicsemi.android.nrftoolbox.parser.RSCMeasurementParser;

public class RSCManager extends BleManager<RSCManagerCallbacks> {
	private static final byte INSTANTANEOUS_STRIDE_LENGTH_PRESENT = 0x01; // 1 bit
	private static final byte TOTAL_DISTANCE_PRESENT = 0x02; // 1 bit
	private static final byte WALKING_OR_RUNNING_STATUS_BITS = 0x04; // 1 bit

	/** Running Speed and Cadence Measurement service UUID */
	public final static UUID RUNNING_SPEED_AND_CADENCE_SERVICE_UUID = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
	/** Running Speed and Cadence Measurement characteristic UUID */
	private static final UUID RSC_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A53-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic mRSCMeasurementCharacteristic;

	public RSCManager(final Context context) {
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
			requests.add(Request.newEnableNotificationsRequest(mRSCMeasurementCharacteristic));
			return requests;
		}

		@Override
		public boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(RUNNING_SPEED_AND_CADENCE_SERVICE_UUID);
			if (service != null) {
				mRSCMeasurementCharacteristic = service.getCharacteristic(RSC_MEASUREMENT_CHARACTERISTIC_UUID);
			}
			return mRSCMeasurementCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			mRSCMeasurementCharacteristic = null;
		}

		@Override
		public void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			Logger.a(mLogSession, "\"" + RSCMeasurementParser.parse(characteristic) + "\" received");

			// Decode the new data
			int offset = 0;
			final int flags = characteristic.getValue()[offset]; // 1 byte
			offset += 1;

			final boolean islmPresent = (flags & INSTANTANEOUS_STRIDE_LENGTH_PRESENT) > 0;
			final boolean tdPreset = (flags & TOTAL_DISTANCE_PRESENT) > 0;
			final boolean running = (flags & WALKING_OR_RUNNING_STATUS_BITS) > 0;

			final float instantaneousSpeed = (float) characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset) / 256.0f; // 1/256 m/s in [m/s]
			offset += 2;

			final int instantaneousCadence = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset); // [SPM]
			offset += 1;

			float instantaneousStrideLength = RSCManagerCallbacks.NOT_AVAILABLE;
			if (islmPresent) {
				instantaneousStrideLength = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset); // [cm]
				offset += 2;
			}

			float totalDistance = RSCManagerCallbacks.NOT_AVAILABLE;
			if (tdPreset) {
				totalDistance = (float) characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset) / 10.0f; // 1/10 m in [m]
				//offset += 4;
			}

			// Notify listener about the new measurement
			mCallbacks.onMeasurementReceived(gatt.getDevice(), instantaneousSpeed, instantaneousCadence, totalDistance, instantaneousStrideLength,
					running ? RSCManagerCallbacks.ACTIVITY_RUNNING : RSCManagerCallbacks.ACTIVITY_WALKING);
		}
	};
}
