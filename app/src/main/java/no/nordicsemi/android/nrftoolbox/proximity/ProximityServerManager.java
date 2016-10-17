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
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;

import java.util.UUID;

import no.nordicsemi.android.error.GattError;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.parser.AlertLevelParser;
import no.nordicsemi.android.nrftoolbox.profile.multiconnect.IDeviceLogger;
import no.nordicsemi.android.nrftoolbox.utility.ParserUtils;

public class ProximityServerManager  {
	private final String TAG = "ProximityServerManager";

	/** Immediate Alert service UUID */
	public final static UUID IMMEDIATE_ALERT_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
	/** Linkloss service UUID */
	public final static UUID LINKLOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
	/** Alert Level characteristic UUID */
	private static final UUID ALERT_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");

	private final static byte[] HIGH_ALERT = { 0x02 };
	private final static byte[] MILD_ALERT = { 0x01 };
	private final static byte[] NO_ALERT = { 0x00 };

	private BluetoothGattServer mBluetoothGattServer;
	private ProximityServerManagerCallbacks mCallbacks;
	private IDeviceLogger mLogger;
	private Handler mHandler;
	private OnServerOpenCallback mOnServerOpenCallback;

	public interface OnServerOpenCallback {
		/** Method called when the GATT server was created and all services were added successfully. */
		void onGattServerOpen();
	}

	public ProximityServerManager(final ProximityServerManagerCallbacks callbacks) {
		mHandler = new Handler();
		mCallbacks = callbacks;
	}

	public void setLogger(final IDeviceLogger logger) {
		mLogger = logger;
	}

	public void openGattServer(final Context context, final OnServerOpenCallback callback) {
		mOnServerOpenCallback = callback;

		final BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothGattServer = manager.openGattServer(context, mGattServerCallbacks);
		addImmediateAlertService();
	}

	public void closeGattServer() {
		if (mBluetoothGattServer != null) {
			mBluetoothGattServer.close();
			mBluetoothGattServer = null;
			mOnServerOpenCallback = null;
		}
	}

	public void cancelConnection(final  BluetoothDevice device) {
		if (mBluetoothGattServer != null) {
			mLogger.log(device, LogContract.Log.Level.VERBOSE, "[Server] Cancelling server connection...");
			mLogger.log(device, LogContract.Log.Level.DEBUG, "server.cancelConnection(device)");
			mBluetoothGattServer.cancelConnection(device);
		}
	}

	private void addImmediateAlertService() {
		/*
		 * This method must be called in UI thread. It works fine on Nexus devices but if called from other thread (e.g. from onServiceAdded in gatt server callback) it hangs the app.
		 */
		final BluetoothGattCharacteristic alertLevel = new BluetoothGattCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
				BluetoothGattCharacteristic.PERMISSION_WRITE);
		alertLevel.setValue(NO_ALERT);
		final BluetoothGattService immediateAlertService = new BluetoothGattService(IMMEDIATE_ALERT_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
		immediateAlertService.addCharacteristic(alertLevel);
		mBluetoothGattServer.addService(immediateAlertService);
	}

	private void addLinklossService() {
		/*
		 * This method must be called in UI thread. It works fine on Nexus devices but if called from other thread (e.g. from onServiceAdded in gatt server callback) it hangs the app.
		 */
		final BluetoothGattCharacteristic linklossAlertLevel = new BluetoothGattCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE
				| BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);
		linklossAlertLevel.setValue(HIGH_ALERT);
		final BluetoothGattService linklossService = new BluetoothGattService(LINKLOSS_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
		linklossService.addCharacteristic(linklossAlertLevel);
		mBluetoothGattServer.addService(linklossService);
	}

	private final BluetoothGattServerCallback mGattServerCallbacks = new BluetoothGattServerCallback() {
		@Override
		public void onServiceAdded(final int status, final BluetoothGattService service) {
			// Adding another service from callback thread fails on Samsung S4 with Android 4.3
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (IMMEDIATE_ALERT_SERVICE_UUID.equals(service.getUuid())) {
						addLinklossService();
					} else if (mOnServerOpenCallback != null) {
						mOnServerOpenCallback.onGattServerOpen();
						mOnServerOpenCallback = null;
					}
				}
			});
		}

		@Override
		public void onConnectionStateChange(final BluetoothDevice device, final int status, final int newState) {
			mLogger.log(device, LogContract.Log.Level.DEBUG, "[Server callback] Connection state changed with status: " + status + " and new state: " + stateToString(newState) + " (" + newState + ")");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothGatt.STATE_CONNECTED) {
					mLogger.log(device, LogContract.Log.Level.INFO, "[Server] Device with address " + device.getAddress() + " connected");
				} else {
					mLogger.log(device, LogContract.Log.Level.INFO, "[Server] Device disconnected");
					mCallbacks.onAlarmStopped(device);
				}
			} else {
				mLogger.log(device, LogContract.Log.Level.ERROR, "[Server] Error " + status + " (0x" + Integer.toHexString(status) + "): " + GattError.parseConnectionError(status));
			}
		}

		@Override
		public void onCharacteristicReadRequest(final BluetoothDevice device, final int requestId, final int offset, final BluetoothGattCharacteristic characteristic) {
			mLogger.log(device, LogContract.Log.Level.DEBUG, "[Server callback] Read request for characteristic " + characteristic.getUuid() + " (requestId=" + requestId + ", offset=" + offset + ")");
			mLogger.log(device, LogContract.Log.Level.INFO, "[Server] READ request for characteristic " + characteristic.getUuid() + " received");

			byte[] value = characteristic.getValue();
			if (value != null && offset > 0) {
				byte[] offsetValue = new byte[value.length - offset];
				System.arraycopy(value, offset, offsetValue, 0, offsetValue.length);
				value = offsetValue;
			}
			if (value != null)
				mLogger.log(device, LogContract.Log.Level.DEBUG, "server.sendResponse(GATT_SUCCESS, value=" + ParserUtils.parseDebug(value) + ")");
			else
				mLogger.log(device, LogContract.Log.Level.DEBUG, "server.sendResponse(GATT_SUCCESS, value=null)");
			mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
			mLogger.log(device, LogContract.Log.Level.VERBOSE, "[Server] Response sent");
		}

		@Override
		public void onCharacteristicWriteRequest(final BluetoothDevice device, final int requestId, final BluetoothGattCharacteristic characteristic, final boolean preparedWrite,
												 final boolean responseNeeded, final int offset, final byte[] value) {
			mLogger.log(device, LogContract.Log.Level.DEBUG, "[Server callback] Write request to characteristic " + characteristic.getUuid()
					+ " (requestId=" + requestId + ", prepareWrite=" + preparedWrite + ", responseNeeded=" + responseNeeded + ", offset=" + offset + ", value=" + ParserUtils.parseDebug(value) + ")");
			final String writeType = !responseNeeded ? "WRITE NO RESPONSE" : "WRITE COMMAND";
			mLogger.log(device, LogContract.Log.Level.INFO, "[Server] " + writeType + " request for characteristic " + characteristic.getUuid() + " received, value: " + ParserUtils.parse(value));

			if (offset == 0) {
				characteristic.setValue(value);
			} else {
				final byte[] currentValue = characteristic.getValue();
				final byte[] newValue = new byte[currentValue.length + value.length];
				System.arraycopy(currentValue, 0, newValue, 0, currentValue.length);
				System.arraycopy(value, 0, newValue, offset, value.length);
				characteristic.setValue(newValue);
			}

			if (!preparedWrite && value != null && value.length == 1) { // small validation
				if (value[0] != NO_ALERT[0]) {
					mLogger.log(device, LogContract.Log.Level.APPLICATION, "[Server] Immediate alarm request received: " + AlertLevelParser.parse(characteristic));
					mCallbacks.onAlarmTriggered(device);
				} else {
					mLogger.log(device, LogContract.Log.Level.APPLICATION, "[Server] Immediate alarm request received: OFF");
					mCallbacks.onAlarmStopped(device);
				}
			}

			mLogger.log(device, LogContract.Log.Level.DEBUG, "server.sendResponse(GATT_SUCCESS, offset=" + offset + ", value=" + ParserUtils.parseDebug(value) + ")");
			mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
			mLogger.log(device, LogContract.Log.Level.VERBOSE, "[Server] Response sent");
		}

		@Override
		public void onDescriptorReadRequest(final BluetoothDevice device, final int requestId, final int offset, final BluetoothGattDescriptor descriptor) {
			mLogger.log(device, LogContract.Log.Level.DEBUG, "[Server callback] Write request to descriptor " + descriptor.getUuid() + " (requestId=" + requestId + ", offset=" + offset + ")");
			mLogger.log(device, LogContract.Log.Level.INFO, "[Server] READ request for descriptor " + descriptor.getUuid() + " received");
			// This method is not supported
			mLogger.log(device, LogContract.Log.Level.WARNING, "[Server] Operation not supported");
			mLogger.log(device, LogContract.Log.Level.DEBUG, "[Server] server.sendResponse(GATT_REQUEST_NOT_SUPPORTED)");
			mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, offset, null);
			mLogger.log(device, LogContract.Log.Level.VERBOSE, "[Server] Response sent");
		}

		@Override
		public void onDescriptorWriteRequest(final BluetoothDevice device, final int requestId, final BluetoothGattDescriptor descriptor, final boolean preparedWrite,
											 final boolean responseNeeded, final int offset, final byte[] value) {
			mLogger.log(device, LogContract.Log.Level.DEBUG, "[Server callback] Write request to descriptor " + descriptor.getUuid()
					+ " (requestId=" + requestId + ", prepareWrite=" + preparedWrite + ", responseNeeded=" + responseNeeded + ", offset=" + offset + ", value=" + ParserUtils.parse(value) + ")");
			mLogger.log(device, LogContract.Log.Level.INFO, "[Server] READ request for descriptor " + descriptor.getUuid() + " received");
			// This method is not supported
			mLogger.log(device, LogContract.Log.Level.WARNING, "[Server] Operation not supported");
			mLogger.log(device, LogContract.Log.Level.DEBUG, "[Server] server.sendResponse(GATT_REQUEST_NOT_SUPPORTED)");
			mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, offset, null);
			mLogger.log(device, LogContract.Log.Level.VERBOSE, "[Server] Response sent");
		}

		@Override
		public void onExecuteWrite(final BluetoothDevice device, final int requestId, final boolean execute) {
			mLogger.log(device, LogContract.Log.Level.DEBUG, "[Server callback] Execute write request (requestId=" + requestId + ", execute=" + execute + ")");
			// This method is not supported
			mLogger.log(device, LogContract.Log.Level.WARNING, "[Server] Operation not supported");
			mLogger.log(device, LogContract.Log.Level.DEBUG, "[Server] server.sendResponse(GATT_REQUEST_NOT_SUPPORTED)");
			mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, 0, null);
			mLogger.log(device, LogContract.Log.Level.VERBOSE, "[Server] Response sent");
		}
	};

	/**
	 * Converts the connection state to String value
	 * @param state the connection state
	 * @return state as String
	 */
	private String stateToString(final int state) {
		switch (state) {
			case BluetoothProfile.STATE_CONNECTED:
				return "CONNECTED";
			case BluetoothProfile.STATE_CONNECTING:
				return "CONNECTING";
			case BluetoothProfile.STATE_DISCONNECTING:
				return "DISCONNECTING";
			default:
				return "DISCONNECTED";
		}
	}
}
