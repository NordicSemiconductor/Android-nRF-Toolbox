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
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.UUID;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.common.data.alert.AlertLevelData;
import no.nordicsemi.android.ble.error.GattError;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.battery.BatteryManager;
import no.nordicsemi.android.nrftoolbox.parser.AlertLevelParser;

@SuppressWarnings("WeakerAccess")
class ProximityManager extends BatteryManager<ProximityManagerCallbacks> {
	/** Link Loss service UUID. */
	final static UUID LINK_LOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
	/** Immediate Alert service UUID. */
	private final static UUID IMMEDIATE_ALERT_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
	/** Alert Level characteristic UUID. */
	private static final UUID ALERT_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic mAlertLevelCharacteristic, mLinkLossCharacteristic;
	private boolean mAlertOn;

	ProximityManager(final Context context) {
		super(context);
	}

	@Override
	protected boolean shouldAutoConnect() {
		return true;
	}

	@NonNull
	@Override
	protected BatteryManagerGattCallback getGattCallback() {
		return mGattCallback;
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery,
	 * receiving indication, etc.
	 */
	private final BatteryManagerGattCallback mGattCallback = new BatteryManagerGattCallback() {

		@Override
		protected void initialize() {
			super.initialize();
			writeCharacteristic(mLinkLossCharacteristic, AlertLevelData.highAlert())
					.done(device -> log(Log.INFO, "Link loss alert level set"))
					.fail((device, status) -> log(Log.WARN, "Failed to set link loss level: " + status))
					.enqueue();
		}

		@Override
		protected boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService llService = gatt.getService(LINK_LOSS_SERVICE_UUID);
			if (llService != null) {
				mLinkLossCharacteristic = llService.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID);
			}
			return mLinkLossCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
			super.isOptionalServiceSupported(gatt);
			final BluetoothGattService iaService = gatt.getService(IMMEDIATE_ALERT_SERVICE_UUID);
			if (iaService != null) {
				mAlertLevelCharacteristic = iaService.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID);
			}
			return mAlertLevelCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			super.onDeviceDisconnected();
			mAlertLevelCharacteristic = null;
			mLinkLossCharacteristic = null;
			// Reset the alert flag
			mAlertOn = false;
		}
	};

	/**
	 * Toggles the immediate alert on the target device.
	 */
	public void toggleImmediateAlert() {
		writeImmediateAlert(!mAlertOn);
	}

	/**
	 * Writes the HIGH ALERT or NO ALERT command to the target device.
	 *
	 * @param on true to enable the alarm on proximity tag, false to disable it.
	 */
	public void writeImmediateAlert(final boolean on) {
		if (!isConnected())
			return;

		writeCharacteristic(mAlertLevelCharacteristic, on ? AlertLevelData.highAlert() : AlertLevelData.noAlert())
				.before(device -> log(Log.VERBOSE,
						on ? "Setting alarm to HIGH..." : "Disabling alarm..."))
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION,
						"\"" + AlertLevelParser.parse(data) + "\" sent"))
				.done(device -> {
					mAlertOn = on;
					mCallbacks.onRemoteAlarmSwitched(device, on);
				})
				.fail((device, status) -> log(Log.WARN,
						status == FailCallback.REASON_NULL_ATTRIBUTE ?
								"Alert Level characteristic not found" :
								GattError.parse(status)))
				.enqueue();
	}

	/**
	 * Returns true if the alert has been enabled on the proximity tag, false otherwise.
	 */
	boolean isAlertEnabled() {
		return mAlertOn;
	}
}
