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
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.parser.AlertLevelParser;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;
import no.nordicsemi.android.nrftoolbox.utility.ParserUtils;

public class ProximityManager extends BleManager<ProximityManagerCallbacks> {
	private final String TAG = "ProximityManager";

	/** Immediate Alert service UUID */
	public final static UUID IMMEDIATE_ALERT_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
	/** Linkloss service UUID */
	public final static UUID LINKLOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
	/** Alert Level characteristic UUID */
	private static final UUID ALERT_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");

	private final static byte[] HIGH_ALERT = { 0x02 };
	private final static byte[] NO_ALERT = { 0x00 };

	private BluetoothGattCharacteristic mAlertLevelCharacteristic, mLinklossCharacteristic;
	private BluetoothGattServer mBluetoothGattServer;
	private BluetoothDevice mDeviceToConnect;
	private Handler mHandler;

	public ProximityManager(Context context) {
		super(context);
		mHandler = new Handler();
	}

	@Override
	protected boolean shouldAutoConnect() {
		return true;
	}

	private void openGattServer(Context context, BluetoothManager manager) {
		mBluetoothGattServer = manager.openGattServer(context, mGattServerCallbacks);
	}

	private void closeGattServer() {
		if (mBluetoothGattServer != null) {
			// mBluetoothGattServer.cancelConnection(mBluetoothGatt.getDevice()); // FIXME this method does not cancel the connection
			mBluetoothGattServer.close(); // FIXME This method does not cause BluetoothGattServerCallback#onConnectionStateChange(newState=DISCONNECTED) to be called on Nexus phones.
			mBluetoothGattServer = null;
		}
	}

	private void addImmediateAlertService() {
		/*
		 * This method must be called in UI thread. It works fine on Nexus devices but if called from other thread (f.e. from onServiceAdded in gatt server callback) it hangs the app. 
		 */
		final BluetoothGattCharacteristic alertLevel = new BluetoothGattCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
				BluetoothGattCharacteristic.PERMISSION_WRITE);
		alertLevel.setValue(HIGH_ALERT);
		final BluetoothGattService immediateAlertService = new BluetoothGattService(IMMEDIATE_ALERT_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
		immediateAlertService.addCharacteristic(alertLevel);
		mBluetoothGattServer.addService(immediateAlertService);
	}

	private void addLinklossService() {
		/*
		 * This method must be called in UI thread. It works fine on Nexus devices but if called from other thread (f.e. from onServiceAdded in gatt server callback) it hangs the app. 
		 */
		final BluetoothGattCharacteristic linklossAlertLevel = new BluetoothGattCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE
				| BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_WRITE);
		linklossAlertLevel.setValue(HIGH_ALERT);
		final BluetoothGattService linklossService = new BluetoothGattService(LINKLOSS_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
		linklossService.addCharacteristic(linklossAlertLevel);
		mBluetoothGattServer.addService(linklossService);
	}

	private final BluetoothGattServerCallback mGattServerCallbacks = new BluetoothGattServerCallback() {

		@Override
		public void onServiceAdded(final int status, final BluetoothGattService service) {
			Logger.v(mLogSession, "[Server] Service " + service.getUuid() + " added");

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// Adding another service from callback thread fails on Samsung S4 with Android 4.3
					if (IMMEDIATE_ALERT_SERVICE_UUID.equals(service.getUuid()))
						addLinklossService();
					else {
						Logger.i(mLogSession, "[Proximity Server] Gatt server started");
						ProximityManager.super.connect(mDeviceToConnect);
						mDeviceToConnect = null;
					}
				}
			});
		}

		@Override
		public void onConnectionStateChange(final BluetoothDevice device, final int status, final int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
				Logger.i(mLogSession, "[Server] Device with address " + device.getAddress() + " connected");
			} else {
				if (newState == BluetoothGatt.STATE_DISCONNECTED) {
					Logger.i(mLogSession, "[Server] Device disconnected");
				} else {
					Logger.e(mLogSession, "[Server] Connection state changed with error " + status);
				}
			}
		}

		@Override
		public void onCharacteristicReadRequest(final BluetoothDevice device, final int requestId, final int offset, final BluetoothGattCharacteristic characteristic) {
			Logger.i(mLogSession, "[Server] Read request for characteristic " + characteristic.getUuid() + " (requestId = " + requestId + ", offset = " + offset + ")");
			Logger.v(mLogSession, "[Server] Sending response: SUCCESS");
			Logger.d(mLogSession, "[Server] sendResponse(GATT_SUCCESS, " + ParserUtils.parse(characteristic.getValue()) + ")");
			mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
		}

		@Override
		public void onCharacteristicWriteRequest(final BluetoothDevice device, final int requestId, final BluetoothGattCharacteristic characteristic, final boolean preparedWrite,
												 final boolean responseNeeded, final int offset, final byte[] value) {
			Logger.i(mLogSession, "[Server] Write request to characteristic " + characteristic.getUuid() + " (requestId = " + requestId + ", value = " + ParserUtils.parse(value) + ", offset = " + offset + ")");
			characteristic.setValue(value);

			if (value != null && value.length == 1) { // small validation
				if (value[0] != NO_ALERT[0]) {
					Logger.a(mLogSession, "[Server] Immediate alarm request received: " + AlertLevelParser.parse(characteristic));
					mCallbacks.onAlarmTriggered();
				} else {
					Logger.a(mLogSession, "[Server] Immediate alarm request received: OFF");
					mCallbacks.onAlarmStopped();
				}
			}
			if (responseNeeded) {
				Logger.v(mLogSession, "[Server] Sending response: SUCCESS");
				Logger.d(mLogSession, "[Server] sendResponse(GATT_SUCCESS)");
				mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
			}
		}

		@Override
		public void onDescriptorReadRequest(final BluetoothDevice device, final int requestId, final int offset, final BluetoothGattDescriptor descriptor) {
			Logger.i(mLogSession, "[Server] Write request to descriptor " + descriptor.getUuid() + " (requestId = " + requestId + ", offset = " + offset + ")");
			// This method is not supported
			Logger.v(mLogSession, "[Server] Sending response: REQUEST_NOT_SUPPORTED");
			Logger.d(mLogSession, "[Server] sendResponse(GATT_REQUEST_NOT_SUPPORTED)");
			mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, offset, null);
		}

		@Override
		public void onDescriptorWriteRequest(final BluetoothDevice device, final int requestId, final BluetoothGattDescriptor descriptor, final boolean preparedWrite,
											 final boolean responseNeeded, final int offset, final byte[] value) {
			Logger.i(mLogSession, "[Server] Write request to descriptor " + descriptor.getUuid() + " (requestId = " + requestId + ", value = " + ParserUtils.parse(value) + ", offset = " + offset + ")");
			// This method is not supported
			Logger.v(mLogSession, "[Server] Sending response: REQUEST_NOT_SUPPORTED");
			Logger.d(mLogSession, "[Server] sendResponse(GATT_REQUEST_NOT_SUPPORTED)");
			mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, offset, null);
		}

		@Override
		public void onExecuteWrite(final BluetoothDevice device, final int requestId, final boolean execute) {
			Logger.i(mLogSession, "[Server] Execute write request (requestId = " + requestId + ")");
			// This method is not supported
			Logger.v(mLogSession, "[Server] Sending response: REQUEST_NOT_SUPPORTED");
			Logger.d(mLogSession, "[Server] sendResponse(GATT_REQUEST_NOT_SUPPORTED)");
			mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, 0, null);
		}
	};

	@Override
	public void connect(final BluetoothDevice device) {
		// Should we use the GATT Server?
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		final boolean useGattServer = preferences.getBoolean(ProximityActivity.PREFS_GATT_SERVER_ENABLED, true);

		if (useGattServer) {
			// Save the device that we want to connect to. First we will create a GATT Server
			mDeviceToConnect = device;

			final BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
			try {
				DebugLogger.d(TAG, "[Server] Starting Gatt server...");
				Logger.v(mLogSession, "[Server] Starting Gatt server...");
				openGattServer(getContext(), bluetoothManager);
				addImmediateAlertService();
				// the BluetoothGattServerCallback#onServiceAdded callback will proceed further operations
			} catch (final Exception e) {
				// On Nexus 4&7 with Android 4.4 (build KRT16S) sometimes creating Gatt Server fails. There is a Null Pointer Exception thrown from addCharacteristic method.
				Logger.e(mLogSession, "[Server] Gatt server failed to start");
				Log.e(TAG, "Creating Gatt Server failed", e);
			}
		} else {
			super.connect(device);
		}
	}

	@Override
	public boolean disconnect() {
		final boolean result = super.disconnect();
		closeGattServer();
		return result;
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
		protected Queue<Request> initGatt(final BluetoothGatt gatt) {
			final LinkedList<Request> requests = new LinkedList<>();
			requests.push(Request.newWriteRequest(mLinklossCharacteristic, HIGH_ALERT));
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
		}
	};

	public void writeImmediateAlertOn() {
		Logger.a(mLogSession, "Immediate alarm request: ON");
		if (mAlertLevelCharacteristic != null) {
			mAlertLevelCharacteristic.setValue(HIGH_ALERT);
			writeCharacteristic(mAlertLevelCharacteristic);
		} else {
			DebugLogger.w(TAG, "Immediate Alert Level Characteristic is not found");
		}
	}

	public void writeImmediateAlertOff() {
		Logger.a(mLogSession, "Immediate alarm request: OFF");
		if (mAlertLevelCharacteristic != null) {
			mAlertLevelCharacteristic.setValue(NO_ALERT);
			writeCharacteristic(mAlertLevelCharacteristic);
		} else {
			DebugLogger.w(TAG, "Immediate Alert Level Characteristic is not found");
		}
	}

	@Override
	public void close() {
		super.close();

		if (mBluetoothGattServer != null) {
			mBluetoothGattServer.close();
			mBluetoothGattServer = null;
		}
	}
}
