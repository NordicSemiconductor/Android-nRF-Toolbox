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

import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

public class ProximityManager implements BleManager<ProximityManagerCallbacks> {
	private final String TAG = "ProximityManager";

	private ProximityManagerCallbacks mCallbacks;
	private BluetoothGattServer mBluetoothGattServer;
	private BluetoothGatt mBluetoothGatt;
	private BluetoothDevice mDeviceToConnect;
	private Context mContext;
	private final Handler mHandler;
	private ILogSession mLogSession;
	private Ringtone mRingtoneNotification;
	private Ringtone mRingtoneAlarm;

	public final static UUID IMMEIDIATE_ALERT_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
	public final static UUID LINKLOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");

	private static final UUID ALERT_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");

	private final static UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
	private final static UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

	private final static String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
	private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
	private final static String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
	private final static String ERROR_WRITE_CHARACTERISTIC = "Error on writing characteristic";
	private final static String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";

	private final static int HIGH_ALERT = 2;
	private final static int NO_ALERT = 0;

	private BluetoothGattCharacteristic mAlertLevelCharacteristic, mLinklossCharacteristic, mBatteryCharacteristic;

	private boolean userDisconnectedFlag = false;

	public ProximityManager(Context context) {
		initializeAlarm(context);

		mHandler = new Handler();

		// Register bonding broadcast receiver
		final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		context.registerReceiver(mBondingBroadcastReceiver, filter);
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
		BluetoothGattCharacteristic alertLevel = new BluetoothGattCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
				BluetoothGattCharacteristic.PERMISSION_WRITE);
		alertLevel.setValue(HIGH_ALERT, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
		BluetoothGattService immediateAlertService = new BluetoothGattService(IMMEIDIATE_ALERT_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
		immediateAlertService.addCharacteristic(alertLevel);
		mBluetoothGattServer.addService(immediateAlertService);
	}

	private void addLinklossService() {
		/*
		 * This method must be called in UI thread. It works fine on Nexus devices but if called from other thread (f.e. from onServiceAdded in gatt server callback) it hangs the app. 
		 */
		BluetoothGattCharacteristic linklossAlertLevel = new BluetoothGattCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE
				| BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_WRITE);
		linklossAlertLevel.setValue(HIGH_ALERT, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
		BluetoothGattService linklossService = new BluetoothGattService(LINKLOSS_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
		linklossService.addCharacteristic(linklossAlertLevel);
		mBluetoothGattServer.addService(linklossService);
	}

	private final BluetoothGattServerCallback mGattServerCallbacks = new BluetoothGattServerCallback() {
		@Override
		public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
			DebugLogger.d(TAG, "[Proximity Server] onCharacteristicReadRequest " + device.getName());
		}

		@Override
		public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset,
				byte[] value) {
			DebugLogger.d(TAG, "[Proximity Server] onCharacteristicWriteRequest " + device.getName());
			final int receivedValue = value[0];
			if (receivedValue != NO_ALERT) {
				Logger.i(mLogSession, "[Proximity Server] Immediate alarm request received: ON");
				playAlarm();
			} else {
				Logger.i(mLogSession, "[Proximity Server] Immediate alarm request received: OFF");
				stopAlarm();
			}
		}

		@Override
		public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
			DebugLogger.d(TAG, "[Proximity Server] onConnectionStateChange " + device.getName() + " status: " + status + " new state: " + newState);
		}

		@Override
		public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
			DebugLogger.d(TAG, "[Proximity Server] onDescriptorReadRequest " + device.getName());
		}

		@Override
		public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
			DebugLogger.d(TAG, "[Proximity Server] onDescriptorWriteRequest " + device.getName());
		}

		@Override
		public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
			DebugLogger.d(TAG, "[Proximity Server] onExecuteWrite " + device.getName());
		}

		@Override
		public void onServiceAdded(final int status, final BluetoothGattService service) {
			DebugLogger.d(TAG, "[Proximity Server] onServiceAdded " + service.getUuid());

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// adding another service from callback thread fails on Samsung S4 with Android 4.3
					if (IMMEIDIATE_ALERT_SERVICE_UUID.equals(service.getUuid()))
						addLinklossService();
					else {
						DebugLogger.d(TAG, "[Proximity Server] Gatt server started");
						Logger.i(mLogSession, "[Proximity Server] Gatt server started");
						if (mBluetoothGatt == null) {
							mBluetoothGatt = mDeviceToConnect.connectGatt(mContext, false, mGattCallback);
							mDeviceToConnect = null;
						} else {
							mBluetoothGatt.connect();
						}
					}
				}
			});
		}
	};

	/**
	 * Callbacks for activity {HTSActivity} that implements HTSManagerCallbacks interface activity use this method to register itself for receiving callbacks
	 */
	@Override
	public void setGattCallbacks(ProximityManagerCallbacks callbacks) {
		mCallbacks = callbacks;
	}

	/**
	 * Sets the log session that can be used to log events
	 * 
	 * @param logSession
	 */
	public void setLogger(ILogSession logSession) {
		mLogSession = logSession;
	}

	@Override
	public void connect(Context context, BluetoothDevice device) {
		mContext = context;
		// save the device that we want to connect to
		mDeviceToConnect = device;

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (preferences.getBoolean(ProximityActivity.PREFS_GATT_SERVER_ENABLED, true)) {
			final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
			try {
				DebugLogger.d(TAG, "[Proximity Server] Starting Gatt server...");
				Logger.v(mLogSession, "[Proximity Server] Starting Gatt server...");
				openGattServer(context, bluetoothManager);
				addImmediateAlertService();
				// the BluetoothGattServerCallback#onServiceAdded callback will proceed further operations
			} catch (final Exception e) {
				// On Nexus 4&7 with Android 4.4 (build KRT16S) sometimes creating Gatt Server fails. There is a Null Pointer Exception thrown from addCharacteristic method.
				Logger.e(mLogSession, "[Proximity Server] Gatt server failed to start");
				Log.e(TAG, "Creating Gatt Server failed", e);
			}
		} else {
			if (mBluetoothGatt == null) {
				mBluetoothGatt = mDeviceToConnect.connectGatt(context, false, mGattCallback);
				mDeviceToConnect = null;
			} else {
				mBluetoothGatt.connect();
			}
		}
	}

	@Override
	public void disconnect() {
		if (mBluetoothGatt != null) {
			userDisconnectedFlag = true;
			mBluetoothGatt.disconnect();
			stopAlarm();
			closeGattServer();
		}
	}

	private void initializeAlarm(Context context) {
		final Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		mRingtoneAlarm = RingtoneManager.getRingtone(context, alarmUri);
		mRingtoneAlarm.setStreamType(AudioManager.STREAM_ALARM);

		final Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		mRingtoneNotification = RingtoneManager.getRingtone(context, notification);
	}

	private void playNotification() {
		DebugLogger.d(TAG, "playNotification");
		mRingtoneNotification.play();
	}

	private void playAlarm() {
		DebugLogger.d(TAG, "playAlarm");
		final AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(AudioManager.STREAM_ALARM, am.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		mRingtoneAlarm.play();
	}

	private void stopAlarm() {
		DebugLogger.d(TAG, "stopAlarm");
		mRingtoneAlarm.stop();
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
	 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					DebugLogger.d(TAG, "Device connected");
					mBluetoothGatt.discoverServices();
					//This will send callback to ProximityActivity when device get connected
					mCallbacks.onDeviceConnected();
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					DebugLogger.d(TAG, "Device disconnected");
					if (userDisconnectedFlag) {
						mCallbacks.onDeviceDisconnected();
						userDisconnectedFlag = false;
					} else {
						playNotification();
						mCallbacks.onLinklossOccur();
					}
				}
			} else {
				mCallbacks.onError(ERROR_CONNECTION_STATE_CHANGE, status);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				final List<BluetoothGattService> services = gatt.getServices();
				for (BluetoothGattService service : services) {
					if (service.getUuid().equals(IMMEIDIATE_ALERT_SERVICE_UUID)) {
						DebugLogger.d(TAG, "Immediate Alert service is found");
						mAlertLevelCharacteristic = service.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID);
					} else if (service.getUuid().equals(LINKLOSS_SERVICE_UUID)) {
						DebugLogger.d(TAG, "Linkloss service is found");
						mLinklossCharacteristic = service.getCharacteristic(ALERT_LEVEL_CHARACTERISTIC_UUID);
					} else if (service.getUuid().equals(BATTERY_SERVICE_UUID)) {
						DebugLogger.d(TAG, "Battery service is found");
						mBatteryCharacteristic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID);
					}
				}
				if (mLinklossCharacteristic == null) {
					mCallbacks.onDeviceNotSupported();
					gatt.disconnect();
				} else {
					mCallbacks.onServicesDiscovered(mAlertLevelCharacteristic != null);
					writeLinklossAlertLevel(HIGH_ALERT);
				}
			} else {
				mCallbacks.onError(ERROR_DISCOVERY_SERVICE, status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (characteristic.getUuid().equals(BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
					int batteryValue = characteristic.getValue()[0];
					mCallbacks.onBatteryValueReceived(batteryValue);
				}
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					DebugLogger.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					mCallbacks.onError(ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				mCallbacks.onError(ERROR_READ_CHARACTERISTIC, status);
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (characteristic.getUuid().equals(ALERT_LEVEL_CHARACTERISTIC_UUID)) {
					if (mBatteryCharacteristic != null) {
						readBatteryLevel();
					}
				}
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					DebugLogger.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					mCallbacks.onError(ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				mCallbacks.onError(ERROR_WRITE_CHARACTERISTIC, status);
			}
		}
	};

	private final BroadcastReceiver mBondingBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
			final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

			// skip other devices
			if (!device.getAddress().equals(mBluetoothGatt.getDevice().getAddress()))
				return;

			DebugLogger.i(TAG, "Bond state changed for: " + device.getName() + " new state: " + bondState + " previous: " + previousBondState);

			if (bondState == BluetoothDevice.BOND_BONDING) {
				mCallbacks.onBondingRequired();
				return;
			}
			if (bondState == BluetoothDevice.BOND_BONDED) {
				if (mLinklossCharacteristic != null) {
					writeLinklossAlertLevel(HIGH_ALERT);
				}
				mCallbacks.onBonded();
			}
		}
	};

	private void readBatteryLevel() {
		if (mBatteryCharacteristic != null) {
			DebugLogger.d(TAG, "reading battery characteristic");
			mBluetoothGatt.readCharacteristic(mBatteryCharacteristic);
		} else {
			DebugLogger.w(TAG, "Battery Level Characteristic is null");
		}
	}

	@SuppressWarnings("unused")
	private void readLinklossAlertLevel() {
		if (mLinklossCharacteristic != null) {
			DebugLogger.d(TAG, "reading linkloss alert level characteristic");
			mBluetoothGatt.readCharacteristic(mLinklossCharacteristic);
		} else {
			DebugLogger.w(TAG, "Linkloss Alert Level Characteristic is null");
		}
	}

	private void writeLinklossAlertLevel(int alertLevel) {
		if (mLinklossCharacteristic != null) {
			DebugLogger.d(TAG, "writing linkloss alert level characteristic");
			mLinklossCharacteristic.setValue(alertLevel, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
			mBluetoothGatt.writeCharacteristic(mLinklossCharacteristic);
		} else {
			DebugLogger.w(TAG, "Linkloss Alert Level Characteristic is not found");
		}
	}

	public void writeImmediateAlertOn() {
		if (mAlertLevelCharacteristic != null) {
			DebugLogger.d(TAG, "writing Immediate alert characteristic On");
			mAlertLevelCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
			mAlertLevelCharacteristic.setValue(HIGH_ALERT, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
			mBluetoothGatt.writeCharacteristic(mAlertLevelCharacteristic);
		} else {
			DebugLogger.w(TAG, "Immediate Alert Level Characteristic is not found");
		}
	}

	public void writeImmediateAlertOff() {
		if (mAlertLevelCharacteristic != null) {
			DebugLogger.d(TAG, "writing Immediate alert characteristic Off");
			mAlertLevelCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
			mAlertLevelCharacteristic.setValue(NO_ALERT, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
			mBluetoothGatt.writeCharacteristic(mAlertLevelCharacteristic);
		} else {
			DebugLogger.w(TAG, "Immediate Alert Level Characteristic is not found");
		}
	}

	@Override
	public void closeBluetoothGatt() {
		try {
			mContext.unregisterReceiver(mBondingBroadcastReceiver);
		} catch (Exception e) {
			// the receiver must have been not registered or unregistered before
		}
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
		if (mBluetoothGattServer != null) {
			mBluetoothGattServer.close();
			mBluetoothGattServer = null;
		}
		mCallbacks = null;
		mLogSession = null;
		mRingtoneAlarm = mRingtoneNotification = null;
	}
}
