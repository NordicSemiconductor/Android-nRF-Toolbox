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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.Calendar;
import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.Request;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.parser.BloodPressureMeasurementParser;
import no.nordicsemi.android.nrftoolbox.parser.IntermediateCuffPressureParser;

public class BPMManager extends BleManager<BPMManagerCallbacks> {
	/** Blood Pressure service UUID */
	public final static UUID BP_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
	/** Blood Pressure Measurement characteristic UUID */
	private static final UUID BPM_CHARACTERISTIC_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb");
	/** Intermediate Cuff Pressure characteristic UUID */
	private static final UUID ICP_CHARACTERISTIC_UUID = UUID.fromString("00002A36-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic mBPMCharacteristic, mICPCharacteristic;

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

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving notification, etc
	 */
	private final BleManagerGattCallback  mGattCallback = new BleManagerGattCallback() {

		@Override
		protected Deque<Request> initGatt(final BluetoothGatt gatt) {
			final LinkedList<Request> requests = new LinkedList<>();
			if (mICPCharacteristic != null)
				requests.add(Request.newEnableNotificationsRequest(mICPCharacteristic));
			requests.add(Request.newEnableIndicationsRequest(mBPMCharacteristic));
			return requests;
		}

		@Override
		protected boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
			BluetoothGattService service = gatt.getService(BP_SERVICE_UUID);
			if (service != null) {
				mBPMCharacteristic = service.getCharacteristic(BPM_CHARACTERISTIC_UUID);
				mICPCharacteristic = service.getCharacteristic(ICP_CHARACTERISTIC_UUID);
			}
			return mBPMCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(final BluetoothGatt gatt) {
			return mICPCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			mICPCharacteristic = null;
			mBPMCharacteristic = null;
		}

		@Override
		protected void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			// Intermediate Cuff Pressure characteristic read
			Logger.a(mLogSession, "\"" + IntermediateCuffPressureParser.parse(characteristic) + "\" received");

			parseBPMValue(gatt, characteristic);
		}

		@Override
		protected void onCharacteristicIndicated(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			// Blood Pressure Measurement characteristic read
			Logger.a(mLogSession, "\"" + BloodPressureMeasurementParser.parse(characteristic) + "\" received");

			parseBPMValue(gatt, characteristic);
		}

		private void parseBPMValue(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			// Both BPM and ICP have the same structure.

			// first byte - flags
			int offset = 0;
			final int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset++);
			// See BPMManagerCallbacks.UNIT_* for unit options
			final int unit = flags & 0x01;
			final boolean timestampPresent = (flags & 0x02) > 0;
			final boolean pulseRatePresent = (flags & 0x04) > 0;

			if (BPM_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
				// following bytes - systolic, diastolic and mean arterial pressure
				final float systolic = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
				final float diastolic = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset + 2);
				final float meanArterialPressure = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset + 4);
				offset += 6;
				mCallbacks.onBloodPressureMeasurementRead(gatt.getDevice(), systolic, diastolic, meanArterialPressure, unit);
			} else if (ICP_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
				// following bytes - cuff pressure. Diastolic and MAP are unused
				final float cuffPressure = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
				offset += 6;
				mCallbacks.onIntermediateCuffPressureRead(gatt.getDevice(), cuffPressure, unit);
			}

			// parse timestamp if present
			if (timestampPresent) {
				final Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.YEAR, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset));
				calendar.set(Calendar.MONTH, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2) - 1); // months are 1-based
				calendar.set(Calendar.DAY_OF_MONTH, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 3));
				calendar.set(Calendar.HOUR_OF_DAY, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 4));
				calendar.set(Calendar.MINUTE, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 5));
				calendar.set(Calendar.SECOND, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 6));
				offset += 7;
				mCallbacks.onTimestampRead(gatt.getDevice(), calendar);
			} else
				mCallbacks.onTimestampRead(gatt.getDevice(), null);

			// parse pulse rate if present
			if (pulseRatePresent) {
				final float pulseRate = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
				// offset += 2;
				mCallbacks.onPulseRateRead(gatt.getDevice(), pulseRate);
			} else
				mCallbacks.onPulseRateRead(gatt.getDevice(), -1.0f);
		}
	};
}
