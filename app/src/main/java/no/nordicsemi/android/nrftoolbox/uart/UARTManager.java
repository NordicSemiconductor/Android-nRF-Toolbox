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

package no.nordicsemi.android.nrftoolbox.uart;

import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

public class UARTManager implements BleManager<UARTManagerCallbacks> {
	private final static String TAG = "UARTManager";

	private final static UUID UART_SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
	/** TX characteristic */
	private final static UUID UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
	/** RX characteristic */
	private final static UUID UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
	/** Client configuration descriptor that will allow us to enable notifications and indications */
	private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	private final static String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
	private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
	private final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";

	private BluetoothGattCharacteristic mTXCharacteristic, mRXCharacteristic;

	private UARTManagerCallbacks mCallbacks;
	private BluetoothGatt mBluetoothGatt;
	private final Context mContext;

	public UARTManager(final Context context) {
		mContext = context;
	}

	@Override
	public void setGattCallbacks(final UARTManagerCallbacks callbacks) {
		mCallbacks = callbacks;
	}

	@Override
	public void connect(final Context context, final BluetoothDevice device) {
		if (mBluetoothGatt == null) {
			mBluetoothGatt = device.connectGatt(mContext, true, mGattCallback);
		} else {
			mBluetoothGatt.connect();
		}
	}

	@Override
	public void disconnect() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
		}
	}

	public void send(final String text) {
		if (mTXCharacteristic != null) {
			mTXCharacteristic.setValue(text);
			mBluetoothGatt.writeCharacteristic(mTXCharacteristic);
		}
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
	 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					DebugLogger.d(TAG, "Device connected");
					mBluetoothGatt.discoverServices();
					//This will send callback to RSCActivity when device get connected
					mCallbacks.onDeviceConnected();
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					DebugLogger.d(TAG, "Device disconnected");

					mCallbacks.onDeviceDisconnected();
					closeBluetoothGatt();
				}
			} else {
				mCallbacks.onError(ERROR_CONNECTION_STATE_CHANGE, status);
			}
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				final List<BluetoothGattService> services = gatt.getServices();
				for (BluetoothGattService service : services) {
					if (service.getUuid().equals(UART_SERVICE_UUID)) {
						DebugLogger.d(TAG, "UART service is found");
						mTXCharacteristic = service.getCharacteristic(UART_TX_CHARACTERISTIC_UUID);
						mRXCharacteristic = service.getCharacteristic(UART_RX_CHARACTERISTIC_UUID);
					}
				}
				if (mTXCharacteristic == null || mRXCharacteristic == null) {
					mCallbacks.onDeviceNotSupported();
					gatt.disconnect();
				} else {
					mCallbacks.onServicesDiscovered(false /* more characteristics not supported */);

					// We have discovered services, let's start notifications on RX characteristics
					enableRXNotification(gatt);
				}
			} else {
				mCallbacks.onError(ERROR_DISCOVERY_SERVICE, status);
			}
		}

		@Override
		public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// do nothing
			} else {
				mCallbacks.onError(ERROR_WRITE_DESCRIPTOR, status);
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				final String data = characteristic.getStringValue(0);
				mCallbacks.onDataSent(data);
			} else {
				mCallbacks.onError(ERROR_WRITE_DESCRIPTOR, status);
			}
		}

		@Override
		public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			final String data = characteristic.getStringValue(0);
			mCallbacks.onDataReceived(data);
		}
	};

	/**
	 * Enabling notification on RX Characteristic
	 */
	private void enableRXNotification(final BluetoothGatt gatt) {
		gatt.setCharacteristicNotification(mRXCharacteristic, true);
		final BluetoothGattDescriptor descriptor = mRXCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		gatt.writeDescriptor(descriptor);
	}

	@Override
	public void closeBluetoothGatt() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
			mTXCharacteristic = null;
			mRXCharacteristic = null;
		}
		mCallbacks = null;
	}

}
