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
import no.nordicsemi.android.nrftoolbox.parser.AlertLevelParser;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class ProximityManager extends BleManager<ProximityManagerCallbacks> {
	private final String TAG = "ProximityManager";

	/** Immediate Alert service UUID */
	public final static UUID IMMEDIATE_ALERT_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
	/** Link Loss service UUID */
	public final static UUID LINKLOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
	/** Alert Level characteristic UUID */
	private static final UUID ALERT_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");

	private final static byte[] HIGH_ALERT = { 0x02 };
	private final static byte[] MILD_ALERT = { 0x01 };
	private final static byte[] NO_ALERT = { 0x00 };

	private BluetoothGattCharacteristic mAlertLevelCharacteristic, mLinklossCharacteristic;
	private boolean mAlertOn;

	public ProximityManager(final Context context) {
		super(context);
	}

	@Override
	protected boolean shouldAutoConnect() {
		return true;
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
			requests.add(Request.newWriteRequest(mLinklossCharacteristic, HIGH_ALERT));
			return requests;
		}

		@Override
		protected boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService llService = gatt.getService(LINKLOSS_SERVICE_UUID);
			if (llService != null) {
				mLinklossCharacteristic = llService.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID);
			}
			return mLinklossCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService iaService = gatt.getService(IMMEDIATE_ALERT_SERVICE_UUID);
			if (iaService != null) {
				mAlertLevelCharacteristic = iaService.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID);
			}
			return mAlertLevelCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			mAlertLevelCharacteristic = null;
			mLinklossCharacteristic = null;
			// Reset the alert flag
			mAlertOn = false;
		}

		@Override
		protected void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			Logger.a(mLogSession, "\"" + AlertLevelParser.parse(characteristic) + "\" sent");
		}
	};

	/**
	 * Toggles the immediate alert on the target device.
	 * @return true if alarm has been enabled, false if disabled
	 */
	public boolean toggleImmediateAlert() {
		writeImmediateAlert(!mAlertOn);
		return mAlertOn;
	}

	/**
	 * Writes the HIGH ALERT or NO ALERT command to the target device
	 * @param on true to enable the alarm on proximity tag, false to disable it
	 */
	public void writeImmediateAlert(final boolean on) {
		if (!isConnected())
			return;

		if (mAlertLevelCharacteristic != null) {
			writeCharacteristic(mAlertLevelCharacteristic, on ? HIGH_ALERT : NO_ALERT);
			mAlertOn = on;
		} else {
			DebugLogger.w(TAG, "Immediate Alert Level Characteristic is not found");
		}
	}

	/**
	 * Returns true if the alert has been enabled on the proximity tag, false otherwise.
	 */
	public boolean isAlertEnabled() {
		return mAlertOn;
	}
}
