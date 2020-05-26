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
package no.nordicsemi.android.nrftoolbox.profile.multiconnect;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.utils.ILogger;
import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.nrftoolbox.profile.LoggableBleManager;

public abstract class BleMulticonnectProfileService extends Service implements BleManagerCallbacks {
	@SuppressWarnings("unused")
	private static final String TAG = "BleMultiProfileService";

	public static final String BROADCAST_CONNECTION_STATE = "no.nordicsemi.android.nrftoolbox.BROADCAST_CONNECTION_STATE";
	public static final String BROADCAST_SERVICES_DISCOVERED = "no.nordicsemi.android.nrftoolbox.BROADCAST_SERVICES_DISCOVERED";
	public static final String BROADCAST_DEVICE_READY = "no.nordicsemi.android.nrftoolbox.DEVICE_READY";
	public static final String BROADCAST_BOND_STATE = "no.nordicsemi.android.nrftoolbox.BROADCAST_BOND_STATE";
	@Deprecated
	public static final String BROADCAST_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.BROADCAST_BATTERY_LEVEL";
	public static final String BROADCAST_ERROR = "no.nordicsemi.android.nrftoolbox.BROADCAST_ERROR";

	public static final String EXTRA_DEVICE = "no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE";
	public static final String EXTRA_CONNECTION_STATE = "no.nordicsemi.android.nrftoolbox.EXTRA_CONNECTION_STATE";
	public static final String EXTRA_BOND_STATE = "no.nordicsemi.android.nrftoolbox.EXTRA_BOND_STATE";
	public static final String EXTRA_SERVICE_PRIMARY = "no.nordicsemi.android.nrftoolbox.EXTRA_SERVICE_PRIMARY";
	public static final String EXTRA_SERVICE_SECONDARY = "no.nordicsemi.android.nrftoolbox.EXTRA_SERVICE_SECONDARY";
	@Deprecated
	public static final String EXTRA_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.EXTRA_BATTERY_LEVEL";
	public static final String EXTRA_ERROR_MESSAGE = "no.nordicsemi.android.nrftoolbox.EXTRA_ERROR_MESSAGE";
	public static final String EXTRA_ERROR_CODE = "no.nordicsemi.android.nrftoolbox.EXTRA_ERROR_CODE";

	public static final int STATE_LINK_LOSS = -1;
	public static final int STATE_DISCONNECTED = 0;
	public static final int STATE_CONNECTED = 1;
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_DISCONNECTING = 3;

	private HashMap<BluetoothDevice, LoggableBleManager<BleManagerCallbacks>> bleManagers;
	private List<BluetoothDevice> managedDevices;
	private Handler handler;

	protected boolean bound;
	private boolean activityIsChangingConfiguration;

	private final BroadcastReceiver bluetoothStateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
			final int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);

			switch (state) {
				case BluetoothAdapter.STATE_ON:
					// On older phones (tested on Nexus 4 with Android 5.0.1) the Bluetooth requires some time
					// after it has been enabled before some operations can start. Starting the GATT server here
					// without a delay is very likely to cause a DeadObjectException from BluetoothManager#openGattServer(...).
					handler.postDelayed(() -> onBluetoothEnabled(), 600);
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
				case BluetoothAdapter.STATE_OFF:
					if (previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF)
						onBluetoothDisabled();
					break;
			}
		}
	};

	public class LocalBinder extends Binder implements ILogger, IDeviceLogger {
		/**
		 * Returns an unmodifiable list of devices managed by the service.
		 * The returned devices do not need to be connected at tha moment. Each of them was however created
		 * using {@link #connect(BluetoothDevice)} method so they might have been connected before and disconnected.
		 * @return unmodifiable list of devices managed by the service
		 */
		public final List<BluetoothDevice> getManagedDevices() {
			return Collections.unmodifiableList(managedDevices);
		}

		/**
		 * Connects to the given device. If the device is already connected this method does nothing.
		 * @param device target Bluetooth device
		 */
		public void connect(final BluetoothDevice device) {
			connect(device, null);
		}

		/**
		 * Adds the given device to managed and stars connecting to it. If the device is already connected this method does nothing.
		 * @param device target Bluetooth device
		 * @param session log session that has to be used by the device
		 */
		@SuppressWarnings("unchecked")
		public void connect(final BluetoothDevice device, final ILogSession session) {
			// If a device is in managed devices it means that it's already connected, or was connected
			// using autoConnect and the link was lost but Android is already trying to connect to it.
			if (managedDevices.contains(device))
				return;
			managedDevices.add(device);

			LoggableBleManager<BleManagerCallbacks> manager = bleManagers.get(device);
			if (manager == null) {
				bleManagers.put(device, manager = initializeManager());
				manager.setGattCallbacks(BleMulticonnectProfileService.this);
			}
			manager.setLogger(session);
			manager.connect(device)
					.retry(2, 100)
					.useAutoConnect(shouldAutoConnect())
					.timeout(10000)
					.fail((d, status) -> {
						managedDevices.remove(device);
						bleManagers.remove(device);
					})
					.enqueue();
		}

		/**
		 * Disconnects the given device and removes the associated BleManager object.
		 * If the list of BleManagers is empty while the last activity unbinds from the service,
		 * the service will stop itself.
		 * @param device target device to disconnect and forget
		 */
		public void disconnect(final BluetoothDevice device) {
			final BleManager manager = bleManagers.get(device);
			if (manager != null && manager.isConnected()) {
				manager.disconnect().enqueue();
			}
			managedDevices.remove(device);
		}

		/**
		 * Returns <code>true</code> if the device is connected to the sensor.
		 * @param device the target device
		 * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
		 */
		public final boolean isConnected(final BluetoothDevice device) {
			final BleManager manager = bleManagers.get(device);
			return manager != null && manager.isConnected();
		}

		/**
		 * Returns <code>true</code> if the device has finished initializing.
		 * @param device the target device
		 * @return <code>true</code> if device is connected to the sensor and has finished
		 * initializing. False otherwise.
		 */
		public final boolean isReady(final BluetoothDevice device) {
			final BleManager manager = bleManagers.get(device);
			return manager != null && manager.isReady();
		}

		/**
		 * Returns the connection state of given device.
		 * @param device the target device
		 * @return the connection state, as in {@link BleManager#getConnectionState()}.
		 */
		@SuppressWarnings("unused")
		public final int getConnectionState(final BluetoothDevice device) {
			final BleManager manager = bleManagers.get(device);
			return manager != null ? manager.getConnectionState() : BluetoothGatt.STATE_DISCONNECTED;
		}

		/**
		 * Returns the last received battery level value.
		 * @param device the device of which battery level should be returned
		 * @return battery value or -1 if no value was received or Battery Level characteristic was not found
		 * @deprecated Keep battery value in your manager instead.
		 */
		@SuppressWarnings("deprecation")
		@Deprecated
		public int getBatteryValue(final BluetoothDevice device) {
			final BleManager manager = bleManagers.get(device);
			if (manager != null)
				return manager.getBatteryValue();
			return 0;
		}

		/**
		 * Sets whether the bound activity if changing configuration or not.
		 * If <code>false</code>, we will turn off battery level notifications in onUnbind(..) method below.
		 * @param changing true if the bound activity is finishing
		 */
		@SuppressWarnings("WeakerAccess")
		public final void setActivityIsChangingConfiguration(final boolean changing) {
			activityIsChangingConfiguration = changing;
		}

		@Override
		public void log(@NonNull final BluetoothDevice device, final int level, final String message) {
			final BleManager manager = bleManagers.get(device);
			if (manager != null)
				manager.log(level, message);
		}

		@Override
		public void log(@NonNull final BluetoothDevice device, final int level, @StringRes final int messageRes, final Object... params) {
			final BleManager manager = bleManagers.get(device);
			if (manager != null)
				manager.log(level, messageRes, params);
		}

		@Override
		public void log(final int level, @NonNull final String message) {
			for (final BleManager manager : bleManagers.values())
				manager.log(level, message);
		}

		@Override
		public void log(final int level, @StringRes final int messageRes, final Object... params) {
			for (final BleManager manager : bleManagers.values())
				manager.log(level, messageRes, params);
		}
	}

	/**
	 * Returns a handler that is created in onCreate().
	 * The handler may be used to postpone execution of some operations or to run them in UI thread.
	 */
	protected Handler getHandler() {
		return handler;
	}

	/**
	 * This method returns whether autoConnect option should be used.
	 *
	 * @return true to use autoConnect feature, false (default) otherwise.
	 */
	protected boolean shouldAutoConnect() {
		return false;
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
		bound = true;
		return getBinder();
	}

	@Override
	public final void onRebind(final Intent intent) {
		bound = true;

		if (!activityIsChangingConfiguration) {
			onRebind();
		}
	}

	/**
	 * Called when the activity has rebound to the service after being recreated.
	 * This method is not called when the activity was killed to be recreated when the phone orientation changed
	 * if prior to being killed called {@link LocalBinder#setActivityIsChangingConfiguration(boolean)} with parameter true.
	 */
	protected void onRebind() {
		// empty default implementation
	}

	@Override
	public final boolean onUnbind(final Intent intent) {
		bound = false;

		if (!activityIsChangingConfiguration) {
			if (!managedDevices.isEmpty()) {
				onUnbind();
			} else {
				// The last activity has disconnected from the service and there are no devices to manage. The service may be stopped.
				stopSelf();
			}
		}

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

	@Override
	public void onCreate() {
		super.onCreate();

		handler = new Handler();

		// Initialize the map of BLE managers
		bleManagers = new HashMap<>();
		managedDevices = new ArrayList<>();

		// Register broadcast receivers
		registerReceiver(bluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

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
	protected abstract LoggableBleManager initializeManager();

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		onServiceStarted();
		// The service does not save addresses of managed devices.
		// A bound activity will be required to add connections again.
		return START_NOT_STICKY;
	}

	/**
	 * Called when the service has been started.
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
		onServiceStopped();
		handler = null;
	}

	/**
	 * Called when the service has been stopped.
	 */
	protected void onServiceStopped() {
		// Unregister broadcast receivers
		unregisterReceiver(bluetoothStateBroadcastReceiver);

		// The managers map may not be empty if the service was killed by the system
		for (final BleManager manager : bleManagers.values()) {
			// Service is being destroyed, no need to disconnect manually.
			manager.close();
			manager.log(Log.INFO, "Service destroyed");
		}
		bleManagers.clear();
		managedDevices.clear();
		bleManagers = null;
		managedDevices = null;
	}

	/**
	 * Method called when Bluetooth Adapter has been disabled.
	 */
	protected void onBluetoothDisabled() {
		// do nothing, BleManagers have their own Bluetooth State broadcast received and will close themselves
	}

	/**
	 * This method is called when Bluetooth Adapter has been enabled. It is also called
	 * after the service was created if Bluetooth Adapter was enabled at that moment.
	 * This method could initialize all Bluetooth related features, for example open the GATT server.
	 * Make sure you call <code>super.onBluetoothEnabled()</code> at this methods reconnects to
	 * devices that were connected before the Bluetooth was turned off.
	 */
	protected void onBluetoothEnabled() {
		for (final BluetoothDevice device : managedDevices) {
			final BleManager manager = bleManagers.get(device);
			if (manager != null && !manager.isConnected())
				manager.connect(device).enqueue();
		}
	}

	@Override
	public void onDeviceConnecting(@NonNull final BluetoothDevice device) {
		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_DEVICE, device);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTING);
		LocalBroadcastManager.getInstance(BleMulticonnectProfileService.this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceConnected(@NonNull final BluetoothDevice device) {
		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_DEVICE, device);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceDisconnecting(@NonNull final BluetoothDevice device) {
		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_DEVICE, device);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTING);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
		// Note: if BleManager#shouldAutoConnect() for this device returned true, this callback will be
		// invoked ONLY when user requested disconnection (using Disconnect button). If the device
		// disconnects due to a link loss, the onLinkLossOccurred(BluetoothDevice) method will be called instead.

		// We no longer want to keep the device in the service
		managedDevices.remove(device);
		// The BleManager is not removed from the HashMap in order to keep the device's log session.
		// bleManagers.remove(device);

		// Do not use the device argument here unless you change calling onDeviceDisconnected from the binder above
		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_DEVICE, device);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

		// When user disconnected the last device while the activity was not bound the service can be stopped
		if (!bound && managedDevices.isEmpty()) {
			stopSelf();
		}
	}

	@Override
	public void onLinkLossOccurred(@NonNull final BluetoothDevice device) {
		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_DEVICE, device);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_LINK_LOSS);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
		final Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
		broadcast.putExtra(EXTRA_DEVICE, device);
		broadcast.putExtra(EXTRA_SERVICE_PRIMARY, true);
		broadcast.putExtra(EXTRA_SERVICE_SECONDARY, optionalServicesFound);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceReady(@NonNull final BluetoothDevice device) {
		final Intent broadcast = new Intent(BROADCAST_DEVICE_READY);
		broadcast.putExtra(EXTRA_DEVICE, device);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceNotSupported(@NonNull final BluetoothDevice device) {
		// We don't like this device, remove it from both collections
		managedDevices.remove(device);
		bleManagers.remove(device);

		final Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
		broadcast.putExtra(EXTRA_DEVICE, device);
		broadcast.putExtra(EXTRA_SERVICE_PRIMARY, false);
		broadcast.putExtra(EXTRA_SERVICE_SECONDARY, false);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

		// no need for disconnecting, it will be disconnected by the manager automatically
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onBatteryValueReceived(@NonNull final BluetoothDevice device, final int value) {
		final Intent broadcast = new Intent(BROADCAST_BATTERY_LEVEL);
		broadcast.putExtra(EXTRA_DEVICE, device);
		broadcast.putExtra(EXTRA_BATTERY_LEVEL, value);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onBondingRequired(@NonNull final BluetoothDevice device) {
		showToast(no.nordicsemi.android.nrftoolbox.common.R.string.bonding);

		final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
		broadcast.putExtra(EXTRA_DEVICE, device);
		broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDING);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onBonded(@NonNull final BluetoothDevice device) {
		showToast(no.nordicsemi.android.nrftoolbox.common.R.string.bonded);

		final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
		broadcast.putExtra(EXTRA_DEVICE, device);
		broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onBondingFailed(@NonNull final BluetoothDevice device) {
		showToast(no.nordicsemi.android.nrftoolbox.common.R.string.bonding_failed);

		final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
		broadcast.putExtra(EXTRA_DEVICE, device);
		broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onError(@NonNull final BluetoothDevice device, @NonNull final String message, final int errorCode) {
		final Intent broadcast = new Intent(BROADCAST_ERROR);
		broadcast.putExtra(EXTRA_DEVICE, device);
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
		handler.post(() -> Toast.makeText(BleMulticonnectProfileService.this, messageResId, Toast.LENGTH_SHORT).show());
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 * 
	 * @param message
	 *            a message to be shown
	 */
	protected void showToast(final String message) {
		handler.post(() -> Toast.makeText(BleMulticonnectProfileService.this, message, Toast.LENGTH_SHORT).show());
	}

	/**
	 * Returns the {@link BleManager} object associated with given device, or null if such has not been created.
	 * To create a BleManager call the {@link LocalBinder#connect(BluetoothDevice)} method must be called.
	 * @param device the target device
	 * @return the BleManager or null
	 */
	protected BleManager getBleManager(final BluetoothDevice device) {
		return bleManagers.get(device);
	}

	/**
	 * Returns unmodifiable list of all managed devices. They don't have to be connected at the moment.
	 * @return list of managed devices
	 */
	protected List<BluetoothDevice> getManagedDevices() {
		return Collections.unmodifiableList(managedDevices);
	}

	/**
	 * Returns a list of those managed devices that are connected at the moment.
	 * @return list of connected devices
	 */
	protected List<BluetoothDevice> getConnectedDevices() {
		final List<BluetoothDevice> list = new ArrayList<>();
		for (BluetoothDevice device : managedDevices) {
			final BleManager manager = bleManagers.get(device);
			if (manager != null && manager.isConnected())
				list.add(device);
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Returns <code>true</code> if the device is connected to the sensor.
	 * @param device the target device
	 * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
	 */
	protected boolean isConnected(final BluetoothDevice device) {
		final BleManager manager = bleManagers.get(device);
		return manager != null && manager.isConnected();
	}
}
