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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.UUID;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.common.callback.alert.AlertLevelDataCallback;
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
	final static UUID IMMEDIATE_ALERT_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
	/** Alert Level characteristic UUID. */
	final static UUID ALERT_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");

	// Client characteristics.
	private BluetoothGattCharacteristic alertLevelCharacteristic, linkLossCharacteristic;
	// Server characteristics.
	private BluetoothGattCharacteristic localAlertLevelCharacteristic;
	/** A flag indicating whether the alarm on the connected proximity tag has been activated. */
	private boolean alertOn;

	ProximityManager(final Context context) {
		super(context);
	}

	@NonNull
	@Override
	protected BatteryManagerGattCallback getGattCallback() {
		return new ProximityManagerGattCallback();
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery,
	 * receiving indication, etc.
	 */
	private class ProximityManagerGattCallback extends BatteryManagerGattCallback {

		@Override
		protected void initialize() {
			super.initialize();
			// This callback will be called whenever local Alert Level char is written
			// by a connected proximity tag.
			setWriteCallback(localAlertLevelCharacteristic)
					.with(new AlertLevelDataCallback() {
						@Override
						public void onAlertLevelChanged(@NonNull final BluetoothDevice device, final int level) {
							mCallbacks.onLocalAlarmSwitched(device, level != ALERT_NONE);
						}
					});
			// After connection, set the Link Loss behaviour on the tag.
			writeCharacteristic(linkLossCharacteristic, AlertLevelData.highAlert())
					.done(device -> log(Log.INFO, "Link loss alert level set"))
					.fail((device, status) -> log(Log.WARN, "Failed to set link loss level: " + status))
					.enqueue();
		}

		@Override
		protected void onServerReady(@NonNull final BluetoothGattServer server) {
			final BluetoothGattService immediateAlertService = server.getService(IMMEDIATE_ALERT_SERVICE_UUID);
			if (immediateAlertService != null) {
				localAlertLevelCharacteristic = immediateAlertService.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID);
			}
		}

		@Override
		protected boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService llService = gatt.getService(LINK_LOSS_SERVICE_UUID);
			if (llService != null) {
				linkLossCharacteristic = llService.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID);
			}
			return linkLossCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
			super.isOptionalServiceSupported(gatt);
			final BluetoothGattService iaService = gatt.getService(IMMEDIATE_ALERT_SERVICE_UUID);
			if (iaService != null) {
				alertLevelCharacteristic = iaService.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID);
			}
			return alertLevelCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			super.onDeviceDisconnected();
			alertLevelCharacteristic = null;
			linkLossCharacteristic = null;
			localAlertLevelCharacteristic = null;
			// Reset the alert flag
			alertOn = false;
		}
	}

	/**
	 * Toggles the immediate alert on the target device.
	 */
	public void toggleImmediateAlert() {
		writeImmediateAlert(!alertOn);
	}

	/**
	 * Writes the HIGH ALERT or NO ALERT command to the target device.
	 *
	 * @param on true to enable the alarm on proximity tag, false to disable it.
	 */
	public void writeImmediateAlert(final boolean on) {
		if (!isConnected())
			return;

		writeCharacteristic(alertLevelCharacteristic, on ? AlertLevelData.highAlert() : AlertLevelData.noAlert())
				.before(device -> log(Log.VERBOSE,
						on ? "Setting alarm to HIGH..." : "Disabling alarm..."))
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION,
						"\"" + AlertLevelParser.parse(data) + "\" sent"))
				.done(device -> {
					alertOn = on;
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
		return alertOn;
	}
}
