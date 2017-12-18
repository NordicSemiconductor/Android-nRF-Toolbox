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
package no.nordicsemi.android.nrftoolbox.profile;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.utils.ILogger;
import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.Logger;

public abstract class BleProfileService extends Service implements BleManagerCallbacks {
	@SuppressWarnings("unused")
	private static final String TAG = "BleProfileService";

	public static final String BROADCAST_CONNECTION_STATE = "no.nordicsemi.android.nrftoolbox.BROADCAST_CONNECTION_STATE";
	public static final String BROADCAST_SERVICES_DISCOVERED = "no.nordicsemi.android.nrftoolbox.BROADCAST_SERVICES_DISCOVERED";
	public static final String BROADCAST_DEVICE_READY = "no.nordicsemi.android.nrftoolbox.DEVICE_READY";
	public static final String BROADCAST_BOND_STATE = "no.nordicsemi.android.nrftoolbox.BROADCAST_BOND_STATE";
	public static final String BROADCAST_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.BROADCAST_BATTERY_LEVEL";
	public static final String BROADCAST_ERROR = "no.nordicsemi.android.nrftoolbox.BROADCAST_ERROR";

	/** The parameter passed when creating the service. Must contain the address of the sensor that we want to connect to */
	public static final String EXTRA_DEVICE_ADDRESS = "no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE_ADDRESS";
	/** The key for the device name that is returned in {@link #BROADCAST_CONNECTION_STATE} with state {@link #STATE_CONNECTED}. */
	public static final String EXTRA_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE_NAME";
	public static final String EXTRA_DEVICE = "no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE";
	public static final String EXTRA_LOG_URI = "no.nordicsemi.android.nrftoolbox.EXTRA_LOG_URI";
	public static final String EXTRA_CONNECTION_STATE = "no.nordicsemi.android.nrftoolbox.EXTRA_CONNECTION_STATE";
	public static final String EXTRA_BOND_STATE = "no.nordicsemi.android.nrftoolbox.EXTRA_BOND_STATE";
	public static final String EXTRA_SERVICE_PRIMARY = "no.nordicsemi.android.nrftoolbox.EXTRA_SERVICE_PRIMARY";
	public static final String EXTRA_SERVICE_SECONDARY = "no.nordicsemi.android.nrftoolbox.EXTRA_SERVICE_SECONDARY";
	public static final String EXTRA_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.EXTRA_BATTERY_LEVEL";
	public static final String EXTRA_ERROR_MESSAGE = "no.nordicsemi.android.nrftoolbox.EXTRA_ERROR_MESSAGE";
	public static final String EXTRA_ERROR_CODE = "no.nordicsemi.android.nrftoolbox.EXTRA_ERROR_CODE";

	public static final int STATE_LINK_LOSS = -1;
	public static final int STATE_DISCONNECTED = 0;
	public static final int STATE_CONNECTED = 1;
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_DISCONNECTING = 3;

	private BleManager<BleManagerCallbacks> mBleManager;
	private Handler mHandler;

	protected boolean mBinded;
	private boolean mActivityIsChangingConfiguration;
	private BluetoothDevice mBluetoothDevice;
	private String mDeviceName;
	private ILogSession mLogSession;

	private final BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
			final ILogger logger = getBinder();

			final String stateString = "[Broadcast] Action received: " + BluetoothAdapter.ACTION_STATE_CHANGED + ", state changed to " + state2String(state);
			logger.log(LogContract.Log.Level.DEBUG, stateString);

			switch (state) {
				case BluetoothAdapter.STATE_ON:
					onBluetoothEnabled();
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
				case BluetoothAdapter.STATE_OFF:
					onBluetoothDisabled();
					break;
			}
		}

		private String state2String(final int state) {
			switch (state) {
				case BluetoothAdapter.STATE_TURNING_ON:
					return "TURNING ON";
				case BluetoothAdapter.STATE_ON:
					return "ON";
				case BluetoothAdapter.STATE_TURNING_OFF:
					return "TURNING OFF";
				case BluetoothAdapter.STATE_OFF:
					return "OFF";
				default:
					return "UNKNOWN (" + state + ")";
			}
		}
	};

	public class LocalBinder extends Binder implements ILogger {
		/**
		 * Disconnects from the sensor.
		 */
		public final void disconnect() {
			final int state = mBleManager.getConnectionState();
			if (state == BluetoothGatt.STATE_DISCONNECTED || state == BluetoothGatt.STATE_DISCONNECTING) {
				mBleManager.close();
				onDeviceDisconnected(mBluetoothDevice);
				return;
			}

			mBleManager.disconnect();
		}

		/**
		 * Sets whether the bound activity if changing configuration or not.
		 * If <code>false</code>, we will turn off battery level notifications in onUnbind(..) method below.
		 * @param changing true if the bound activity is finishing
		 */
		public void setActivityIsChangingConfiguration(final boolean changing) {
			mActivityIsChangingConfiguration = changing;
		}

		/**
		 * Returns the device address
		 *
		 * @return device address
		 */
		public String getDeviceAddress() {
			return mBluetoothDevice.getAddress();
		}

		/**
		 * Returns the device name
		 *
		 * @return the device name
		 */
		public String getDeviceName() {
			return mDeviceName;
		}

		/**
		 * Returns the Bluetooth device
		 *
		 * @return the Bluetooth device
		 */
		public BluetoothDevice getBluetoothDevice() {
			return mBluetoothDevice;
		}

		/**
		 * Returns <code>true</code> if the device is connected to the sensor.
		 *
		 * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
		 */
		public boolean isConnected() {
			return mBleManager.isConnected();
		}


		/**
		 * Returns the connection state of given device.
		 * @return the connection state, as in {@link BleManager#getConnectionState()}.
		 */
		public int getConnectionState() {
			return mBleManager.getConnectionState();
		}

		/**
		 * Returns the log session that can be used to append log entries.
		 * The log session is created when the service is being created.
		 * The method returns <code>null</code> if the nRF Logger app was not installed.
		 * 
		 * @return the log session
		 */
		public ILogSession getLogSession() {
			return mLogSession;
		}

		@Override
		public void log(final int level, final String message) {
			Logger.log(mLogSession, level, message);
		}

		@Override
		public void log(final int level, final @StringRes int messageRes, final Object... params) {
			Logger.log(mLogSession, level, messageRes, params);
		}
	}

	/**
	 * Returns a handler that is created in onCreate().
	 * The handler may be used to postpone execution of some operations or to run them in UI thread.
	 */
	protected Handler getHandler() {
		return mHandler;
	}

	/**
	 * Returns the binder implementation. This must return class implementing the additional manager interface that may be used in the bound activity.
	 *
	 * @return the service binder
	 */
	protected LocalBinder getBinder() {
		// default implementation returns the basic binder. You can overwrite the LocalBinder with your own, wider implementation
		return new LocalBinder();
	}

	@Override
	public IBinder onBind(final Intent intent) {
		mBinded = true;
		return getBinder();
	}

	@Override
	public final void onRebind(final Intent intent) {
		mBinded = true;

		if (!mActivityIsChangingConfiguration)
			onRebind();

		if (!mActivityIsChangingConfiguration && mBleManager.isConnected()) {
			// This method will read the Battery Level value, if possible and then try to enable battery notifications (if it has NOTIFY property).
			// If the Battery Level characteristic has only the NOTIFY property, it will only try to enable notifications.
			mBleManager.readBatteryLevel();
		}
	}

	/**
	 * Called when the activity has rebound to the service after being recreated.
	 * This method is not called when the activity was killed to be recreated when the phone orientation changed
	 * if prior to being killed called {@link BleProfileService.LocalBinder#setActivityIsChangingConfiguration(boolean)} with parameter true.
	 */
	protected void onRebind() {
		// empty default implementation
	}

	@Override
	public final boolean onUnbind(final Intent intent) {
		mBinded = false;

		if (!mActivityIsChangingConfiguration)
			onUnbind();

		// When we are connected, but the application is not open, we are not really interested in battery level notifications. But we will still be receiving other values, if enabled.
		if (!mActivityIsChangingConfiguration && mBleManager.isConnected())
			mBleManager.setBatteryNotifications(false);

		// We want the onRebind method be called if anything else binds to it again
		return true;
	}

	/**
	 * Called when the activity has unbound from the service before being finished.
	 * This method is not called when the activity is killed to be recreated when the phone orientation changed.
	 */
	protected void onUnbind() {
		// empty default implementation
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate() {
		super.onCreate();

		mHandler = new Handler();

		// Initialize the manager
		mBleManager = initializeManager();
		mBleManager.setGattCallbacks(this);

		// Register broadcast receivers
		registerReceiver(mBluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

		// Service has now been created
		onServiceCreated();

		// Call onBluetoothEnabled if Bluetooth enabled
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter.isEnabled()) {
			onBluetoothEnabled();
		}
	}

	/**
	 * Called when the service has been created, before the {@link #onBluetoothEnabled()} is called.
	 */
	protected void onServiceCreated() {
		// empty default implementation
	}

	/**
	 * Initializes the Ble Manager responsible for connecting to a single device.
	 * @return a new BleManager object
	 */
	@SuppressWarnings("rawtypes")
	protected abstract BleManager initializeManager();

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		if (intent == null || !intent.hasExtra(EXTRA_DEVICE_ADDRESS))
			throw new UnsupportedOperationException("No device address at EXTRA_DEVICE_ADDRESS key");

		final Uri logUri = intent.getParcelableExtra(EXTRA_LOG_URI);
		mLogSession = Logger.openSession(getApplicationContext(), logUri);
		mDeviceName = intent.getStringExtra(EXTRA_DEVICE_NAME);

		Logger.i(mLogSession, "Service started");

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		final BluetoothAdapter adapter = bluetoothManager.getAdapter();
		final String deviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
		mBluetoothDevice = adapter.getRemoteDevice(deviceAddress);

		mBleManager.setLogger(mLogSession);
		onServiceStarted();
		mBleManager.connect(mBluetoothDevice);
		return START_REDELIVER_INTENT;
	}

	/**
	 * Called when the service has been started. The device name and address are set.
	 * The BLE Manager will try to connect to the device after this method finishes.
	 */
	protected void onServiceStarted() {
		// empty default implementation
	}

	@Override
	public void onTaskRemoved(final Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
		// This method is called when user removed the app from Recents.
		// By default, the service will be killed and recreated immediately after that.
		// However, all managed devices will be lost and devices will be disconnected.
		stopSelf();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Unregister broadcast receivers
		unregisterReceiver(mBluetoothStateBroadcastReceiver);

		// shutdown the manager
		mBleManager.close();
		Logger.i(mLogSession, "Service destroyed");
		mBleManager = null;
		mBluetoothDevice = null;
		mDeviceName = null;
		mLogSession = null;
		mHandler = null;
	}

	/**
	 * Method called when Bluetooth Adapter has been disabled.
	 */
	protected void onBluetoothDisabled() {
		// empty default implementation
	}

	/**
	 * This method is called when Bluetooth Adapter has been enabled and
	 * after the service was created if Bluetooth Adapter was enabled at that moment.
	 * This method could initialize all Bluetooth related features, for example open the GATT server.
	 */
	protected void onBluetoothEnabled() {
		// empty default implementation
	}

	@Override
	public boolean shouldEnableBatteryLevelNotifications(final BluetoothDevice device) {
		// By default the Battery Level notifications will be enabled only the activity is bound.
		return mBinded;
	}

	@Override
	public void onDeviceConnecting(final BluetoothDevice device) {
		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_DEVICE, mBluetoothDevice);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTING);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceConnected(final BluetoothDevice device) {
		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTED);
		broadcast.putExtra(EXTRA_DEVICE, mBluetoothDevice);
		broadcast.putExtra(EXTRA_DEVICE_NAME, mDeviceName);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceDisconnecting(final BluetoothDevice device) {
		// Notify user about changing the state to DISCONNECTING
		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_DEVICE, mBluetoothDevice);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTING);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	/**
	 * This method should return false if the service needs to do some asynchronous work after if has disconnected from the device.
	 * In that case the {@link #stopService()} method must be called when done.
	 * @return true (default) to automatically stop the service when device is disconnected. False otherwise.
	 */
	protected boolean stopWhenDisconnected() {
		return true;
	}

	@Override
	public void onDeviceDisconnected(final BluetoothDevice device) {
		// Note 1: Do not use the device argument here unless you change calling onDeviceDisconnected from the binder above

		// Note 2: if BleManager#shouldAutoConnect() for this device returned true, this callback will be
		// invoked ONLY when user requested disconnection (using Disconnect button). If the device
		// disconnects due to a link loss, the onLinklossOccur(BluetoothDevice) method will be called instead.

		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_DEVICE, mBluetoothDevice);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

		if (stopWhenDisconnected())
			stopService();
	}

	protected void stopService() {
		// user requested disconnection. We must stop the service
		Logger.v(mLogSession, "Stopping service...");
		stopSelf();
	}

	@Override
	public void onLinklossOccur(final BluetoothDevice device) {
		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_DEVICE, mBluetoothDevice);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_LINK_LOSS);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
		final Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
		broadcast.putExtra(EXTRA_DEVICE, mBluetoothDevice);
		broadcast.putExtra(EXTRA_SERVICE_PRIMARY, true);
		broadcast.putExtra(EXTRA_SERVICE_SECONDARY, optionalServicesFound);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceReady(final BluetoothDevice device) {
		final Intent broadcast = new Intent(BROADCAST_DEVICE_READY);
		broadcast.putExtra(EXTRA_DEVICE, mBluetoothDevice);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceNotSupported(final BluetoothDevice device) {
		final Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
		broadcast.putExtra(EXTRA_DEVICE, mBluetoothDevice);
		broadcast.putExtra(EXTRA_SERVICE_PRIMARY, false);
		broadcast.putExtra(EXTRA_SERVICE_SECONDARY, false);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

		// no need for disconnecting, it will be disconnected by the manager automatically
	}

	@Override
	public void onBatteryValueReceived(final BluetoothDevice device, final int value) {
		final Intent broadcast = new Intent(BROADCAST_BATTERY_LEVEL);
		broadcast.putExtra(EXTRA_DEVICE, mBluetoothDevice);
		broadcast.putExtra(EXTRA_BATTERY_LEVEL, value);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onBondingRequired(final BluetoothDevice device) {
		showToast(no.nordicsemi.android.nrftoolbox.common.R.string.bonding);

		final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
		broadcast.putExtra(EXTRA_DEVICE, mBluetoothDevice);
		broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDING);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onBonded(final BluetoothDevice device) {
		showToast(no.nordicsemi.android.nrftoolbox.common.R.string.bonded);

		final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
		broadcast.putExtra(EXTRA_DEVICE, mBluetoothDevice);
		broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onError(final BluetoothDevice device, final String message, final int errorCode) {
		final Intent broadcast = new Intent(BROADCAST_ERROR);
		broadcast.putExtra(EXTRA_DEVICE, mBluetoothDevice);
		broadcast.putExtra(EXTRA_ERROR_MESSAGE, message);
		broadcast.putExtra(EXTRA_ERROR_CODE, errorCode);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 * 
	 * @param messageResId
	 *            an resource id of the message to be shown
	 */
	protected void showToast(final int messageResId) {
		mHandler.post(() -> Toast.makeText(BleProfileService.this, messageResId, Toast.LENGTH_SHORT).show());
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 * 
	 * @param message
	 *            a message to be shown
	 */
	protected void showToast(final String message) {
		mHandler.post(() -> Toast.makeText(BleProfileService.this, message, Toast.LENGTH_SHORT).show());
	}

	/**
	 * Returns the log session that can be used to append log entries. The method returns <code>null</code> if the nRF Logger app was not installed. It is safe to use logger when
	 * {@link #onServiceStarted()} has been called.
	 * 
	 * @return the log session
	 */
	protected ILogSession getLogSession() {
		return mLogSession;
	}

	/**
	 * Returns the device address
	 * 
	 * @return device address
	 */
	protected String getDeviceAddress() {
		return mBluetoothDevice.getAddress();
	}

	/**
	 * Returns the Bluetooth device object
	 *
	 * @return bluetooth device
	 */
	protected BluetoothDevice getBluetoothDevice() {
		return mBluetoothDevice;
	}

	/**
	 * Returns the device name
	 * 
	 * @return the device name
	 */
	protected String getDeviceName() {
		return mDeviceName;
	}

	/**
	 * Returns <code>true</code> if the device is connected to the sensor.
	 * 
	 * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
	 */
	protected boolean isConnected() {
		return mBleManager != null && mBleManager.isConnected();
	}
}
