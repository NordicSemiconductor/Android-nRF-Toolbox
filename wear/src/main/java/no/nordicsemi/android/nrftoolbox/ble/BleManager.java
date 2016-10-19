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

import android.bluetooth.BluetoothAdapter;
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
import android.os.Build;
import android.os.Handler;

import java.util.Deque;
import java.util.LinkedList;
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
 * <li>When initialization complete, the {@link BleManagerCallbacks#onDeviceReady(BluetoothDevice)} callback is called.</li>
 * </ol>
 * </p>
 * <p>Events from all profiles are being logged into the nRF Logger application,
 * which may be downloaded from Google Play: <a href="https://play.google.com/store/apps/details?id=no.nordicsemi.android.log">https://play.google.com/store/apps/details?id=no.nordicsemi.android.log</a></p>
 * <p>The nRF Logger application allows you to see application logs without need to connect it to the computer.</p>
 */
public class BleManager implements BleProfileApi {
	private final static String TAG = "BleManager";

	private final static UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	private final static UUID BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
	private final static UUID BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

	private final static UUID GENERIC_ATTRIBUTE_SERVICE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
	private final static UUID SERVICE_CHANGED_CHARACTERISTIC = UUID.fromString("00002A05-0000-1000-8000-00805f9b34fb");

	private final Object mLock = new Object();

	protected final BleManagerCallbacks mCallbacks;
	private final Context mContext;
	private final Handler mHandler;
	protected BluetoothDevice mBluetoothDevice;
	protected BleProfile mProfile;
	private BluetoothGatt mBluetoothGatt;
	private BleManagerGattCallback mGattCallback;
	/**
	 * This flag is set to false only when the {@link #shouldAutoConnect()} method returns true and the device got disconnected without calling {@link #disconnect()} method.
	 * If {@link #shouldAutoConnect()} returns false (default) this is always set to true.
	 */
	private boolean mUserDisconnected;
	/** Flag set to true when the device is connected. */
	private boolean mConnected;
	private int mConnectionState = BluetoothGatt.STATE_DISCONNECTED;
	/** Last received battery value or -1 if value wasn't received. */
	private int mBatteryValue = -1;

	private final BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
			final int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);

			switch (state) {
				case BluetoothAdapter.STATE_TURNING_OFF:
				case BluetoothAdapter.STATE_OFF:
					if (mConnected && previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF) {
						// The connection is killed by the system, no need to gently disconnect
						mGattCallback.notifyDeviceDisconnected(mBluetoothDevice);
						close();
					}
					break;
			}
		}
	};

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
					mCallbacks.onBondingRequired(device);
					break;
				case BluetoothDevice.BOND_BONDED:
					mCallbacks.onBonded(device);

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
	 * Returns whether to connect to the remote device just once (false) or to add the address to white list of devices
	 * that will be automatically connect as soon as they become available (true). In the latter case, if
	 * Bluetooth adapter is enabled, Android scans periodically for devices from the white list and if a advertising packet
	 * is received from such, it tries to connect to it. When the connection is lost, the system will keep trying to reconnect
	 * to it in. If true is returned, and the connection to the device is lost the {@link BleManagerCallbacks#onLinklossOccur(BluetoothDevice)}
	 * callback is called instead of {@link BleManagerCallbacks#onDeviceDisconnected(BluetoothDevice)}.
	 * <p>This feature works much better on newer Android phone models and many not work on older phones.</p>
	 * <p>This method should only be used with bonded devices, as otherwise the device may change it's address.
	 * It will however work also with non-bonded devices with private static address. A connection attempt to
	 * a device with private resolvable address will fail.</p>
	 *
	 * @return autoConnect flag value
	 */
	protected boolean shouldAutoConnect() {
		return false;
	}

	/**
	 * Connects to the Bluetooth Smart device.
	 *
	 * @param device a device to connect to
	 */
	public void connect(final BluetoothDevice device) {
		if (mConnected)
			return;

		synchronized (mLock) {
			if (mBluetoothGatt != null) {
				mBluetoothGatt.close();
				mBluetoothGatt = null;
			} else {
				// Register bonding broadcast receiver
				mContext.registerReceiver(mBluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
				mContext.registerReceiver(mBondingBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
			}
		}

		final boolean autoConnect = shouldAutoConnect();
		mUserDisconnected = !autoConnect; // We will receive Linkloss events only when the device is connected with autoConnect=true
		mBluetoothDevice = device;
		mConnectionState = BluetoothGatt.STATE_CONNECTING;
		mBluetoothGatt = device.connectGatt(mContext, autoConnect, mGattCallback = new BleManagerGattCallback());
	}

	/**
	 * Disconnects from the device. Does nothing if not connected.
	 *
	 * @return true if device is to be disconnected. False if it was already disconnected.
	 */
	public boolean disconnect() {
		mUserDisconnected = true;

		if (mConnected && mBluetoothGatt != null) {
			mConnectionState = BluetoothGatt.STATE_DISCONNECTING;
			mCallbacks.onDeviceDisconnecting(mBluetoothGatt.getDevice());
			mBluetoothGatt.disconnect();
			return true;
		}
		return false;
	}

	/**
	 * This method returns true if the device is connected. Services could have not been discovered yet.
	 */
	public boolean isConnected() {
		return mConnected;
	}

	/**
	 * Method returns the connection state:
	 * {@link BluetoothGatt#STATE_CONNECTING STATE_CONNECTING},
	 * {@link BluetoothGatt#STATE_CONNECTED STATE_CONNECTED},
	 * {@link BluetoothGatt#STATE_DISCONNECTING STATE_DISCONNECTING},
	 * {@link BluetoothGatt#STATE_DISCONNECTED STATE_DISCONNECTED}
	 * @return the connection state
	 */
	public int getConnectionState() {
		return mConnectionState;
	}

	/**
	 * Returns the last received value of Battery Level characteristic, or -1 if such does not exist, hasn't been read or notification wasn't received yet.
	 * @return the last battery level value in percent
	 */
	public int getBatteryValue() {
		return mBatteryValue;
	}

	/**
	 * Closes and releases resources. May be also used to unregister broadcast listeners.
	 */
	public void close() {
		try {
			mContext.unregisterReceiver(mBluetoothStateBroadcastReceiver);
			mContext.unregisterReceiver(mBondingBroadcastReceiver);
		} catch (Exception e) {
			// the receiver must have been not registered or unregistered before
		}
		synchronized (mLock) {
			if (mBluetoothGatt != null) {
				mBluetoothGatt.close();
				mBluetoothGatt = null;
			}
			mConnected = false;
			mConnectionState = BluetoothGatt.STATE_DISCONNECTED;
			mGattCallback = null;
			mBluetoothDevice = null;
		}
	}

	/**
	 * When the device is bonded and has the Generic Attribute service and the Service Changed characteristic this method enables indications on this characteristic.
	 * In case one of the requirements is not fulfilled this method returns <code>false</code>.
	 *
	 * @return <code>true</code> when the request has been sent, <code>false</code> when the device is not bonded, does not have the Generic Attribute service, the GA service does not have
	 * the Service Changed characteristic or this characteristic does not have the CCCD.
	 */
	private boolean ensureServiceChangedEnabled() {
		final BluetoothGatt gatt = mBluetoothGatt;
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
		return enqueue(Request.newEnableNotificationsRequest(characteristic));
	}

	private boolean internalEnableNotifications(final BluetoothGattCharacteristic characteristic) {
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
		return enqueue(Request.newEnableIndicationsRequest(characteristic));
	}

	private boolean internalEnableIndications(final BluetoothGattCharacteristic characteristic) {
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
		return enqueue(Request.newReadRequest(characteristic));
	}

	private boolean internalReadCharacteristic(final BluetoothGattCharacteristic characteristic) {
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
		return enqueue(Request.newWriteRequest(characteristic, characteristic.getValue()));
	}

	private boolean internalWriteCharacteristic(final BluetoothGattCharacteristic characteristic) {
		final BluetoothGatt gatt = mBluetoothGatt;
		if (gatt == null || characteristic == null)
			return false;

		// Check characteristic property
		final int properties = characteristic.getProperties();
		if ((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0)
			return false;

		return gatt.writeCharacteristic(characteristic);
	}

	@Override
	public final boolean readDescriptor(final BluetoothGattDescriptor descriptor) {
		return enqueue(Request.newReadRequest(descriptor));
	}

	private boolean internalReadDescriptor(final BluetoothGattDescriptor descriptor) {
		final BluetoothGatt gatt = mBluetoothGatt;
		if (gatt == null || descriptor == null)
			return false;

		return gatt.readDescriptor(descriptor);
	}

	@Override
	public final boolean writeDescriptor(final BluetoothGattDescriptor descriptor) {
		return enqueue(Request.newWriteRequest(descriptor, descriptor.getValue()));
	}

	private boolean internalWriteDescriptor(final BluetoothGattDescriptor descriptor) {
		final BluetoothGatt gatt = mBluetoothGatt;
		if (gatt == null || descriptor == null)
			return false;

		// There was a bug in Android up to 6.0 where the descriptor was written using parent characteristic write type, instead of always Write With Response,
		// as the spec says.
		final BluetoothGattCharacteristic parentCharacteristic = descriptor.getCharacteristic();
		final int originalWriteType = parentCharacteristic.getWriteType();
		parentCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
		final boolean result = gatt.writeDescriptor(descriptor);
		parentCharacteristic.setWriteType(originalWriteType);
		return result;
	}

	@Override
	public final boolean readBatteryLevel() {
		return enqueue(Request.newReadBatteryLevelRequest());
	}

	private boolean internalReadBatteryLevel() {
		final BluetoothGatt gatt = mBluetoothGatt;
		if (gatt == null)
			return false;

		final BluetoothGattService batteryService = gatt.getService(BATTERY_SERVICE);
		if (batteryService == null)
			return false;

		final BluetoothGattCharacteristic batteryLevelCharacteristic = batteryService.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC);
		if (batteryLevelCharacteristic == null)
			return false;

		// Check characteristic property
		final int properties = batteryLevelCharacteristic.getProperties();
		if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) == 0)
			return false;

		return internalReadCharacteristic(batteryLevelCharacteristic);
	}

	@Override
	public final boolean setBatteryNotifications(final boolean enable) {
		if (enable)
			return enqueue(Request.newEnableBatteryLevelNotificationsRequest());
		else
			return enqueue(Request.newDisableBatteryLevelNotificationsRequest());
	}

	private boolean internalSetBatteryNotifications(final boolean enable) {
		final BluetoothGatt gatt = mBluetoothGatt;
		if (gatt == null) {
			return false;
		}

		final BluetoothGattService batteryService = gatt.getService(BATTERY_SERVICE);
		if (batteryService == null)
			return false;

		final BluetoothGattCharacteristic batteryLevelCharacteristic = batteryService.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC);
		if (batteryLevelCharacteristic == null)
			return false;

		// Check characteristic property
		final int properties = batteryLevelCharacteristic.getProperties();
		if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
			return false;

		gatt.setCharacteristicNotification(batteryLevelCharacteristic, enable);
		final BluetoothGattDescriptor descriptor = batteryLevelCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
		if (descriptor != null) {
			if (enable) {
				descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			} else {
				descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
			}
			return gatt.writeDescriptor(descriptor);
		}
		return false;
	}

	@Override
	public boolean enqueue(final Request request) {
		if (mGattCallback != null) {
			// Add the new task to the end of the queue
			mGattCallback.mTaskQueue.add(request);
			mGattCallback.nextRequest();
			return true;
		}
		return false;
	}

	private final class BleManagerGattCallback extends BluetoothGattCallback {
		private final static String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
		private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
		private final static String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
		private final static String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
		private final static String ERROR_WRITE_CHARACTERISTIC = "Error on writing characteristic";
		private final static String ERROR_READ_DESCRIPTOR = "Error on reading descriptor";
		private final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";

		private final Queue<Request> mTaskQueue = new LinkedList<>();
		private Deque<Request> mInitQueue;
		private boolean mInitInProgress;
		private boolean mOperationInProgress;

		private void notifyDeviceDisconnected(final BluetoothDevice device) {
			mConnected = false;
			mConnectionState = BluetoothGatt.STATE_DISCONNECTED;
			if (mUserDisconnected) {
				mCallbacks.onDeviceDisconnected(device);
				close();
			} else {
				mCallbacks.onLinklossOccur(device);
				// We are not closing the connection here as the device should try to reconnect automatically.
				// This may be only called when the shouldAutoConnect() method returned true.
			}
			if (mProfile != null)
				mProfile.release();
		}

		private void onError(final BluetoothDevice device, final String message, final int errorCode) {
			mCallbacks.onError(device, message, errorCode);
			if (mProfile != null)
				mProfile.onError(message, errorCode);
		}

		@Override
		public final void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
				// Notify the parent activity/service
				mConnected = true;
				mConnectionState = BluetoothGatt.STATE_CONNECTED;
				mCallbacks.onDeviceConnected(gatt.getDevice());

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
					mOperationInProgress = true; // no more calls are possible
					notifyDeviceDisconnected(gatt.getDevice());
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

					// Before we start executing the initialization queue some other tasks need to be done.
					if (mInitQueue == null)
						mInitQueue = new LinkedList<>();

					// Note, that operations are added in reverse order to the front of the queue.

					// 3. Enable Battery Level notifications if required (if this char. does not exist, this operation will be skipped)
					if (mCallbacks.shouldEnableBatteryLevelNotifications(gatt.getDevice()))
						mInitQueue.addFirst(Request.newEnableBatteryLevelNotificationsRequest());
					// 2. Read Battery Level characteristic (if such does not exist, this will be skipped)
					mInitQueue.addFirst(Request.newReadBatteryLevelRequest());
					// 1. On devices running Android 4.3-6.0 the Service Changed characteristic needs to be enabled by the app (for bonded devices)
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
						mInitQueue.addFirst(Request.newEnableServiceChangedIndicationsRequest());

					mOperationInProgress = false;
					nextRequest();
				} else {
					mCallbacks.onDeviceNotSupported(gatt.getDevice());
					disconnect();
				}
			} else {
				DebugLogger.e(TAG, "onServicesDiscovered error " + status);
				onError(gatt.getDevice(), ERROR_DISCOVERY_SERVICE, status);
			}
		}

		@Override
		public final void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (isBatteryLevelCharacteristic(characteristic)) {
					final int batteryValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
					mBatteryValue = batteryValue;
					mProfile.onBatteryValueReceived(gatt, batteryValue);
				} else {
					// The value has been read. Notify the profile and proceed with the initialization queue.
					mProfile.onCharacteristicRead(gatt, characteristic);
				}
				mOperationInProgress = false;
				nextRequest();
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					// This should never happen but it used to: http://stackoverflow.com/a/20093695/2115352
					DebugLogger.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					onError(gatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				DebugLogger.e(TAG, "onCharacteristicRead error " + status);
				onError(gatt.getDevice(), ERROR_READ_CHARACTERISTIC, status);
			}
		}

		@Override
		public final void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// The value has been written. Notify the profile and proceed with the initialization queue.
				mProfile.onCharacteristicWrite(gatt, characteristic);
				mOperationInProgress = false;
				nextRequest();
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					// This should never happen but it used to: http://stackoverflow.com/a/20093695/2115352
					DebugLogger.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					onError(gatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				DebugLogger.e(TAG, "onCharacteristicWrite error " + status);
				onError(gatt.getDevice(), ERROR_WRITE_CHARACTERISTIC, status);
			}
		}

		@Override
		public void onDescriptorRead(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// The value has been read. Notify the profile and proceed with the initialization queue.
				mProfile.onDescriptorRead(gatt, descriptor);
				mOperationInProgress = false;
				nextRequest();
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					// This should never happen but it used to: http://stackoverflow.com/a/20093695/2115352
					DebugLogger.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					onError(gatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				DebugLogger.e(TAG, "onDescriptorRead error " + status);
				onError(gatt.getDevice(), ERROR_READ_DESCRIPTOR, status);
			}
		}

		@Override
		public final void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// The value has been written. Notify the profile and proceed with the initialization queue.
				mProfile.onDescriptorWrite(gatt, descriptor);
				mOperationInProgress = false;
				nextRequest();
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					// This should never happen but it used to: http://stackoverflow.com/a/20093695/2115352
					DebugLogger.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					onError(gatt.getDevice(), ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				DebugLogger.e(TAG, "onDescriptorWrite error " + status);
				onError(gatt.getDevice(), ERROR_WRITE_DESCRIPTOR, status);
			}
		}

		@Override
		public final void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			if (isBatteryLevelCharacteristic(characteristic)) {
				final int batteryValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
				mBatteryValue = batteryValue;
				mProfile.onBatteryValueReceived(gatt, batteryValue);
			} else {
				final BluetoothGattDescriptor cccd = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
				final boolean notifications = cccd == null || cccd.getValue() == null || cccd.getValue().length != 2 || cccd.getValue()[0] == 0x01;

				if (notifications) {
					mProfile.onCharacteristicNotified(gatt, characteristic);
				} else { // indications
					mProfile.onCharacteristicIndicated(gatt, characteristic);
				}
			}
		}

		/**
		 * Executes the next request. If the last element from the initialization queue has been executed
		 * the {@link BleManagerCallbacks#onDeviceReady(BluetoothDevice)} callback is called.
		 */
		private void nextRequest() {
			if (mOperationInProgress)
				return;

			// Get the first request from the init queue
			Request request = mInitQueue != null ? mInitQueue.poll() : null;

			// Are we done with initializing?
			if (request == null) {
				if (mInitInProgress) {
					mInitQueue = null; // release the queue
					mInitInProgress = false;
					mCallbacks.onDeviceReady(mBluetoothDevice);
				}
				// If so, we can continue with the task queue
				request = mTaskQueue.poll();
				if (request == null) {
					// Nothing to be done for now
					return;
				}
			}

			mOperationInProgress = true;
			boolean result = false;
			switch (request.type) {
				case READ: {
					result = internalReadCharacteristic(request.characteristic);
					break;
				}
				case WRITE: {
					final BluetoothGattCharacteristic characteristic = request.characteristic;
					characteristic.setValue(request.value);
					characteristic.setWriteType(request.writeType);
					result = internalWriteCharacteristic(characteristic);
					break;
				}
				case READ_DESCRIPTOR: {
					result = internalReadDescriptor(request.descriptor);
					break;
				}
				case WRITE_DESCRIPTOR: {
					final BluetoothGattDescriptor descriptor = request.descriptor;
					descriptor.setValue(request.value);
					result = internalWriteDescriptor(descriptor);
					break;
				}
				case ENABLE_NOTIFICATIONS: {
					result = internalEnableNotifications(request.characteristic);
					break;
				}
				case ENABLE_INDICATIONS: {
					result = internalEnableIndications(request.characteristic);
					break;
				}
				case READ_BATTERY_LEVEL: {
					result = internalReadBatteryLevel();
					break;
				}
				case ENABLE_BATTERY_LEVEL_NOTIFICATIONS: {
					result = internalSetBatteryNotifications(true);
					break;
				}
				case DISABLE_BATTERY_LEVEL_NOTIFICATIONS: {
					result = internalSetBatteryNotifications(false);
					break;
				}
				case ENABLE_SERVICE_CHANGED_INDICATIONS: {
					result = ensureServiceChangedEnabled();
					break;
				}
			}
			// The result may be false if given characteristic or descriptor were not found on the device.
			// In that case, proceed with next operation and ignore the one that failed.
			if (!result) {
				mOperationInProgress = false;
				nextRequest();
			}
		}

		/**
		 * Returns true if the characteristic is the Battery Level characteristic.
		 *
		 * @param characteristic the characteristic to be checked
		 * @return true if the characteristic is the Battery Level characteristic.
		 */
		private boolean isBatteryLevelCharacteristic(final BluetoothGattCharacteristic characteristic) {
			if (characteristic == null)
				return false;

			return BATTERY_LEVEL_CHARACTERISTIC.equals(characteristic.getUuid());
		}
	}
}
