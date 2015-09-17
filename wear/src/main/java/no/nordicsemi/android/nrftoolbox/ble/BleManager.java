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
package no.nordicsemi.android.nrftoolbox.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import java.util.Queue;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

/**
 * <p>The BleManager is responsible for managing the low level communication with a Bluetooth Smart device. Please see profiles implementation for an example of use.
 * This base manager has been tested against number of devices and samples from Nordic SDK.</p>
 * <p>The manager handles connection events and initializes the device after establishing the connection.
 * <ol>
 * <li>For bonded devices it ensures that the Service Changed indications, if this characteristic is present, are enabled. Android does not enable them by default,
 * leaving this to the developers.</li>
 * <li>The manager tries to read the Battery Level characteristic. No matter the result of this operation (for example the Battery Level characteristic may not have the READ property)
 * it tries to enable Battery Level notifications, to get battery updates from the device.</li>
 * <li>Afterwards, the manager initializes the device using given queue of commands. See {@link BleProfile#initGatt(BluetoothGatt)} method for more details.</li>
 * <li>When initialization complete, the {@link BleManagerCallbacks#onDeviceReady()} callback is called.</li>
 * </ol>
 * </p>
 * <p>Events from all profiles are being logged into the nRF Logger application,
 * which may be downloaded from Google Play: <a href="https://play.google.com/store/apps/details?id=no.nordicsemi.android.log">https://play.google.com/store/apps/details?id=no.nordicsemi.android.log</a></p>
 * <p>The nRF Logger application allows you to see application logs without need to connect it to the computer.</p>
 */
public class BleManager implements BleProfileApi {
	private final static String TAG = "BleManager";

	private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	private final static UUID GENERIC_ATTRIBUTE_SERVICE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
	private final static UUID SERVICE_CHANGED_CHARACTERISTIC = UUID.fromString("00002A05-0000-1000-8000-00805f9b34fb");

	private final static String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
	private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
	private final static String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
	private final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
	private final static String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";

	protected final BleManagerCallbacks mCallbacks;
	protected BleProfile mProfile;
	private final Context mContext;
	private Handler mHandler;
	private BluetoothGatt mBluetoothGatt;
	private boolean mUserDisconnected;
	private boolean mConnected;

	private BroadcastReceiver mBondingBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
			final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

			// Skip other devices
			if (mBluetoothGatt == null || !device.getAddress().equals(mBluetoothGatt.getDevice().getAddress()))
				return;

			DebugLogger.i(TAG, "Bond state changed for: " + device.getName() + " new state: " + bondState + " previous: " + previousBondState);

			switch (bondState) {
				case BluetoothDevice.BOND_BONDING:
					mCallbacks.onBondingRequired();
					break;
				case BluetoothDevice.BOND_BONDED:
					mCallbacks.onBonded();

					// Start initializing again.
					// In fact, bonding forces additional, internal service discovery (at least on Nexus devices), so this method may safely be used to start this process again.
					mBluetoothGatt.discoverServices();
					break;
			}
		}
	};

	public BleManager(final Context context, final BleManagerCallbacks callbacks) {
		mCallbacks = callbacks;
		mContext = context;
		mHandler = new Handler();
		mUserDisconnected = false;

		// Register bonding broadcast receiver
		context.registerReceiver(mBondingBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
	}

	/**
	 * Returns the Profile API. Profile may be null if service discovery has not been performed or the device does not match any profile.
	 */
	public BleProfile getProfile() {
		return mProfile;
	}

	/**
	 * Returns the context that the manager was created with.
	 *
	 * @return the context
	 */
	@Override
	public Context getContext() {
		return mContext;
	}

	/**
	 * Returns whether to directly connect to the remote device (false) or to automatically connect as soon as the remote
	 * device becomes available (true).
	 *
	 * @return autoConnect flag value
	 */
	protected boolean shouldAutoConnect() {
		return false;
	}

	/**
	 * Connects to the Bluetooth Smart device
	 *
	 * @param device a device to connect to
	 */
	public void connect(final BluetoothDevice device) {
		if (mConnected)
			return;

		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}

		final boolean autoConnect = shouldAutoConnect();
		mUserDisconnected = !autoConnect; // We will receive Linkloss events only when the device is connected with autoConnect=true
		mBluetoothGatt = device.connectGatt(mContext, autoConnect, mGattCallback);
	}

	/**
	 * Disconnects from the device. Does nothing if not connected.
	 * @return true if device is to be disconnected. False if it was already disconnected.
	 */
	public boolean disconnect() {
		mUserDisconnected = true;

		if (mConnected && mBluetoothGatt != null) {
			mCallbacks.onDeviceDisconnecting();
			mBluetoothGatt.disconnect();
			return true;
		}
		return false;
	}

	/**
	 * Closes and releases resources. May be also used to unregister broadcast listeners.
	 */
	public void close() {
		try {
			mContext.unregisterReceiver(mBondingBroadcastReceiver);
		} catch (Exception e) {
			// the receiver must have been not registered or unregistered before
		}
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
		mUserDisconnected = false;
	}

	/**
	 * When the device is bonded and has the Generic Attribute service and the Service Changed characteristic this method enables indications on this characteristic.
	 * In case one of the requirements is not fulfilled this method returns <code>false</code>.
	 *
	 * @param gatt the gatt device with services discovered
	 * @return <code>true</code> when the request has been sent, <code>false</code> when the device is not bonded, does not have the Generic Attribute service, the GA service does not have
	 * the Service Changed characteristic or this characteristic does not have the CCCD.
	 */
	private boolean ensureServiceChangedEnabled(final BluetoothGatt gatt) {
		if (gatt == null)
			return false;

		// The Service Changed indications have sense only on bonded devices
		final BluetoothDevice device = gatt.getDevice();
		if (device.getBondState() != BluetoothDevice.BOND_BONDED)
			return false;

		final BluetoothGattService gaService = gatt.getService(GENERIC_ATTRIBUTE_SERVICE);
		if (gaService == null)
			return false;

		final BluetoothGattCharacteristic scCharacteristic = gaService.getCharacteristic(SERVICE_CHANGED_CHARACTERISTIC);
		if (scCharacteristic == null)
			return false;

		return enableIndications(scCharacteristic);
	}

	@Override
	public final boolean enableNotifications(final BluetoothGattCharacteristic characteristic) {
		final BluetoothGatt gatt = mBluetoothGatt;
		if (gatt == null || characteristic == null)
			return false;

		// Check characteristic property
		final int properties = characteristic.getProperties();
		if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
			return false;

		gatt.setCharacteristicNotification(characteristic, true);
		final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
		if (descriptor != null) {
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			return gatt.writeDescriptor(descriptor);
		}
		return false;
	}

	@Override
	public final boolean enableIndications(final BluetoothGattCharacteristic characteristic) {
		final BluetoothGatt gatt = mBluetoothGatt;
		if (gatt == null || characteristic == null)
			return false;

		// Check characteristic property
		final int properties = characteristic.getProperties();
		if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0)
			return false;

		gatt.setCharacteristicNotification(characteristic, true);
		final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
		if (descriptor != null) {
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
			return gatt.writeDescriptor(descriptor);
		}
		return false;
	}

	@Override
	public final boolean readCharacteristic(final BluetoothGattCharacteristic characteristic) {
		final BluetoothGatt gatt = mBluetoothGatt;
		if (gatt == null || characteristic == null)
			return false;

		// Check characteristic property
		final int properties = characteristic.getProperties();
		if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) == 0)
			return false;

		return gatt.readCharacteristic(characteristic);
	}

	@Override
	public final boolean writeCharacteristic(final BluetoothGattCharacteristic characteristic) {
		final BluetoothGatt gatt = mBluetoothGatt;
		if (gatt == null || characteristic == null)
			return false;

		// Check characteristic property
		final int properties = characteristic.getProperties();
		if ((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0)
			return false;

		return gatt.writeCharacteristic(characteristic);
	}

	public static final class Request {
		private enum Type {
			WRITE,
			READ,
			ENABLE_NOTIFICATIONS,
			ENABLE_INDICATIONS
		}

		private final Type type;
		private final BluetoothGattCharacteristic characteristic;
		private final byte[] value;

		private Request(final Type type, final BluetoothGattCharacteristic characteristic) {
			this.type = type;
			this.characteristic = characteristic;
			this.value = null;
		}

		private Request(final Type type, final BluetoothGattCharacteristic characteristic, final byte[] value) {
			this.type = type;
			this.characteristic = characteristic;
			this.value = value;
		}

		public static Request newReadRequest(final BluetoothGattCharacteristic characteristic) {
			return new Request(Type.READ, characteristic);
		}

		public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value) {
			return new Request(Type.WRITE, characteristic, value);
		}

		public static Request newEnableNotificationsRequest(final BluetoothGattCharacteristic characteristic) {
			return new Request(Type.ENABLE_NOTIFICATIONS, characteristic);
		}

		public static Request newEnableIndicationsRequest(final BluetoothGattCharacteristic characteristic) {
			return new Request(Type.ENABLE_INDICATIONS, characteristic);
		}
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		private Queue<Request> mInitQueue;
		private boolean mInitInProgress;

		private void onError(final String message, final int errorCode) {
			mCallbacks.onError(message, errorCode);
			if (mProfile != null)
				mProfile.onError(message, errorCode);
		}

		@Override
		public final void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
				// Notify the parent activity/service
				mConnected = true;
				mCallbacks.onDeviceConnected();

				/*
				 * The onConnectionStateChange event is triggered just after the Android connects to a device.
				 * In case of bonded devices, the encryption is reestablished AFTER this callback is called.
				 * Moreover, when the device has Service Changed indication enabled, and the list of services has changed (e.g. using the DFU),
				 * the indication is received few milliseconds later, depending on the connection interval.
				 * When received, Android will start performing a service discovery operation itself, internally.
				 *
				 * If the mBluetoothGatt.discoverServices() method would be invoked here, if would returned cached services,
				 * as the SC indication wouldn't be received yet.
				 * Therefore we have to postpone the service discovery operation until we are (almost, as there is no such callback) sure, that it had to be handled.
				 * Our tests has shown that 600 ms is enough. It is important to call it AFTER receiving the SC indication, but not necessarily
				 * after Android finishes the internal service discovery.
				 *
				 * NOTE: This applies only for bonded devices with Service Changed characteristic, but to be sure we will postpone
				 * service discovery for all devices.
				 */
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						// Some proximity tags (e.g. nRF PROXIMITY) initialize bonding automatically when connected.
						if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING) {
							gatt.discoverServices();
						}
					}
				}, 600);
			} else {
				if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					mConnected = false;
					if (mUserDisconnected) {
						mCallbacks.onDeviceDisconnected();
						close();
					} else {
						mCallbacks.onLinklossOccur();
						// We are not closing the connection here as the device should try to reconnect automatically.
						// This may be only called when the shouldAutoConnect() method returned true.
					}
					if (mProfile != null)
						mProfile.release();
					return;
				}

				// TODO Should the disconnect method be called or the connection is still valid? Does this ever happen?
				mProfile.onError(ERROR_CONNECTION_STATE_CHANGE, status);
			}
		}

		@Override
		public final void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				final BleProfile profile = BleProfileProvider.findProfile(gatt);
				if (profile != null) {
					profile.setApi(BleManager.this);
					mProfile = profile;

					// Obtain the queue of initialization requests
					mInitInProgress = true;
					mInitQueue = profile.initGatt(gatt);

					// When the device is bonded and has Service Changed characteristic, the indications must be enabled first.
					// In case this method returns true we have to continue in the onDescriptorWrite callback
					if (ensureServiceChangedEnabled(gatt))
						return;

					// We have discovered services, proceed with the initialization queue.
					nextRequest();
				} else {
					mCallbacks.onDeviceNotSupported();
					disconnect();
				}
			} else {
				DebugLogger.e(TAG, "onServicesDiscovered error " + status);
				onError(ERROR_DISCOVERY_SERVICE, status);
			}
		}

		@Override
		public final void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// The value has been read. Notify the profile and proceed with the initialization queue.
				mProfile.onCharacteristicRead(gatt, characteristic);
				nextRequest();
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					DebugLogger.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					onError(ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				DebugLogger.e(TAG, "onCharacteristicRead error " + status);
				onError(ERROR_READ_CHARACTERISTIC, status);
			}
		}

		@Override
		public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// The value has been written. Notify the profile and proceed with the initialization queue.
				mProfile.onCharacteristicWrite(gatt, characteristic);
				nextRequest();
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					DebugLogger.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					onError(ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				DebugLogger.e(TAG, "onCharacteristicRead error " + status);
				onError(ERROR_READ_CHARACTERISTIC, status);
			}
		}

		@Override
		public final void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// The value has been written. Notify the profile and proceed with the initialization queue.
				mProfile.onDescriptorWrite(gatt, descriptor);
				nextRequest();
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					DebugLogger.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					onError(ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				DebugLogger.e(TAG, "onDescriptorWrite error " + status);
				onError(ERROR_WRITE_DESCRIPTOR, status);
			}
		}

		@Override
		public final void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			final BluetoothGattDescriptor cccd = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
			final boolean notifications = cccd == null || cccd.getValue() == null || cccd.getValue().length != 2 || cccd.getValue()[0] == 0x01;

			if (notifications) {
				mProfile.onCharacteristicNotified(gatt, characteristic);
			} else { // indications
				mProfile.onCharacteristicIndicated(gatt, characteristic);
			}
		}

		/**
		 * Executes the next initialization request. If the last element from the queue has been executed a {@link BleManagerCallbacks#onDeviceReady()} callback is called.
		 */
		private void nextRequest() {
			final Queue<Request> requests = mInitQueue;

			// Get the first request from the queue
			final Request request = requests != null ? requests.poll() : null;

			// Are we done?
			if (request == null) {
				if (mInitInProgress) {
					mInitInProgress = false;
					mCallbacks.onDeviceReady();
				}
				return;
			}

			switch (request.type) {
				case READ: {
					readCharacteristic(request.characteristic);
					break;
				}
				case WRITE: {
					final BluetoothGattCharacteristic characteristic = request.characteristic;
					characteristic.setValue(request.value);
					writeCharacteristic(characteristic);
					break;
				}
				case ENABLE_NOTIFICATIONS: {
					enableNotifications(request.characteristic);
					break;
				}
				case ENABLE_INDICATIONS: {
					enableIndications(request.characteristic);
					break;
				}
			}
		}
	};
}
