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
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.LocalLogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.AppHelpFragment;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

/**
 * <p>
 * The {@link BleProfileServiceReadyActivity} activity is designed to be the base class for profile activities that uses services in order to connect to the
 * device. When user press CONNECT button a service is created and the activity binds to it. The service tries to connect to the service and notifies the
 * activity using Local Broadcasts ({@link LocalBroadcastManager}). See {@link BleProfileService} for messages. If the device is not in range it will listen for
 * it and connect when it become visible. The service exists until user will press DISCONNECT button.
 * </p>
 * <p>
 * When user closes the activity (f.e. by pressing Back button) while being connected, the Service remains working. It's still connected to the device or still
 * listens for it. When entering back to the activity, activity will to bind to the service and refresh UI.
 * </p>
 */
public abstract class BleProfileServiceReadyActivity<E extends BleProfileService.LocalBinder> extends AppCompatActivity implements
		ScannerFragment.OnDeviceSelectedListener, BleManagerCallbacks {
	private static final String TAG = "BleProfileServiceReadyActivity";

	private static final String SIS_DEVICE_NAME = "device_name";
	private static final String SIS_DEVICE = "device";
	private static final String LOG_URI = "log_uri";
	protected static final int REQUEST_ENABLE_BT = 2;

	private E mService;

	private TextView mDeviceNameView;
	private TextView mBatteryLevelView;
	private Button mConnectButton;

	private ILogSession mLogSession;
	private BluetoothDevice mBluetoothDevice;
	private String mDeviceName;

	private final BroadcastReceiver mCommonBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			// Check if the broadcast applies the connected device
			if (!isBroadcastForThisDevice(intent))
				return;

			final BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BleProfileService.EXTRA_DEVICE);
			final String action = intent.getAction();
			switch (action) {
				case BleProfileService.BROADCAST_CONNECTION_STATE: {
					final int state = intent.getIntExtra(BleProfileService.EXTRA_CONNECTION_STATE, BleProfileService.STATE_DISCONNECTED);

					switch (state) {
						case BleProfileService.STATE_CONNECTED: {
							mDeviceName = intent.getStringExtra(BleProfileService.EXTRA_DEVICE_NAME);
							onDeviceConnected(bluetoothDevice);
							break;
						}
						case BleProfileService.STATE_DISCONNECTED: {
							onDeviceDisconnected(bluetoothDevice);
							mDeviceName = null;
							break;
						}
						case BleProfileService.STATE_LINK_LOSS: {
							onLinklossOccur(bluetoothDevice);
							break;
						}
						case BleProfileService.STATE_CONNECTING: {
							onDeviceConnecting(bluetoothDevice);
							break;
						}
						case BleProfileService.STATE_DISCONNECTING: {
							onDeviceDisconnecting(bluetoothDevice);
							break;
						}
						default:
							// there should be no other actions
							break;
					}
					break;
				}
				case BleProfileService.BROADCAST_SERVICES_DISCOVERED: {
					final boolean primaryService = intent.getBooleanExtra(BleProfileService.EXTRA_SERVICE_PRIMARY, false);
					final boolean secondaryService = intent.getBooleanExtra(BleProfileService.EXTRA_SERVICE_SECONDARY, false);

					if (primaryService) {
						onServicesDiscovered(bluetoothDevice, secondaryService);
					} else {
						onDeviceNotSupported(bluetoothDevice);
					}
					break;
				}
				case BleProfileService.BROADCAST_DEVICE_READY: {
					onDeviceReady(bluetoothDevice);
					break;
				}
				case BleProfileService.BROADCAST_BOND_STATE: {
					final int state = intent.getIntExtra(BleProfileService.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
					switch (state) {
						case BluetoothDevice.BOND_BONDING:
							onBondingRequired(bluetoothDevice);
							break;
						case BluetoothDevice.BOND_BONDED:
							onBonded(bluetoothDevice);
							break;
					}
					break;
				}
				case BleProfileService.BROADCAST_BATTERY_LEVEL: {
					final int value = intent.getIntExtra(BleProfileService.EXTRA_BATTERY_LEVEL, -1);
					if (value > 0)
						onBatteryValueReceived(bluetoothDevice, value);
					break;
				}
				case BleProfileService.BROADCAST_ERROR: {
					final String message = intent.getStringExtra(BleProfileService.EXTRA_ERROR_MESSAGE);
					final int errorCode = intent.getIntExtra(BleProfileService.EXTRA_ERROR_CODE, 0);
					onError(bluetoothDevice, message, errorCode);
					break;
				}
			}
		}
	};

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(final ComponentName name, final IBinder service) {
			final E bleService = mService = (E) service;
			mBluetoothDevice = bleService.getBluetoothDevice();
			mLogSession = mService.getLogSession();
			Logger.d(mLogSession, "Activity bound to the service");
			onServiceBinded(bleService);

			// Update UI
			mDeviceName = bleService.getDeviceName();
			mDeviceNameView.setText(mDeviceName);
			mConnectButton.setText(R.string.action_disconnect);

			// And notify user if device is connected
			if (bleService.isConnected()) {
				onDeviceConnected(mBluetoothDevice);
			} else {
				// If the device is not connected it means that either it is still connecting,
				// or the link was lost and service is trying to connect to it (autoConnect=true).
				onDeviceConnecting(mBluetoothDevice);
			}
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			// Note: this method is called only when the service is killed by the system,
			// not when it stops itself or is stopped by the activity.
			// It will be called only when there is critically low memory, in practice never
			// when the activity is in foreground.
			Logger.d(mLogSession, "Activity disconnected from the service");
			mDeviceNameView.setText(getDefaultDeviceName());
			mConnectButton.setText(R.string.action_connect);

			mService = null;
			mDeviceName = null;
			mBluetoothDevice = null;
			mLogSession = null;
			onServiceUnbinded();
		}
	};

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ensureBLESupported();
		if (!isBLEEnabled()) {
			showBLEDialog();
		}

		// Restore the old log session
		if (savedInstanceState != null) {
			final Uri logUri = savedInstanceState.getParcelable(LOG_URI);
			mLogSession = Logger.openSession(getApplicationContext(), logUri);
		}

		// In onInitialize method a final class may register local broadcast receivers that will listen for events from the service
		onInitialize(savedInstanceState);
		// The onCreateView class should... create the view
		onCreateView(savedInstanceState);

		final Toolbar toolbar = findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(toolbar);

		// Common nRF Toolbox view references are obtained here
		setUpView();
		// View is ready to be used
		onViewCreated(savedInstanceState);

		LocalBroadcastManager.getInstance(this).registerReceiver(mCommonBroadcastReceiver, makeIntentFilter());
	}

	@Override
	protected void onResume() {
		super.onResume();

		/*
		 * If the service has not been started before, the following lines will not start it. However, if it's running, the Activity will bind to it and
		 * notified via mServiceConnection.
		 */
		final Intent service = new Intent(this, getServiceClass());
		bindService(service, mServiceConnection, 0); // we pass 0 as a flag so the service will not be created if not exists

		/*
		 * * - When user exited the UARTActivity while being connected, the log session is kept in the service. We may not get it before binding to it so in this
		 * case this event will not be logged (mLogSession is null until onServiceConnected(..) is called). It will, however, be logged after the orientation changes.
		 */
	}

	@Override
	protected void onPause() {
		super.onPause();

		try {
			// We don't want to perform some operations (e.g. disable Battery Level notifications) in the service if we are just rotating the screen.
			// However, when the activity will disappear, we may want to disable some device features to reduce the battery consumption.
			if (mService != null)
				mService.setActivityIsChangingConfiguration(isChangingConfigurations());

			unbindService(mServiceConnection);
			mService = null;

			Logger.d(mLogSession, "Activity unbound from the service");
			onServiceUnbinded();
			mDeviceName = null;
			mBluetoothDevice = null;
			mLogSession = null;
		} catch (final IllegalArgumentException e) {
			// do nothing, we were not connected to the sensor
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommonBroadcastReceiver);
	}

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BleProfileService.BROADCAST_CONNECTION_STATE);
		intentFilter.addAction(BleProfileService.BROADCAST_SERVICES_DISCOVERED);
		intentFilter.addAction(BleProfileService.BROADCAST_DEVICE_READY);
		intentFilter.addAction(BleProfileService.BROADCAST_BOND_STATE);
		intentFilter.addAction(BleProfileService.BROADCAST_BATTERY_LEVEL);
		intentFilter.addAction(BleProfileService.BROADCAST_ERROR);
		return intentFilter;
	}

	/**
	 * Called when activity binds to the service. The parameter is the object returned in {@link Service#onBind(Intent)} method in your service. The method is
	 * called when device gets connected or is created while sensor was connected before. You may use the binder as a sensor interface.
	 */
	protected abstract void onServiceBinded(E binder);

	/**
	 * Called when activity unbinds from the service. You may no longer use this binder because the sensor was disconnected. This method is also called when you
	 * leave the activity being connected to the sensor in the background.
	 */
	protected abstract void onServiceUnbinded();

	/**
	 * Returns the service class for sensor communication. The service class must derive from {@link BleProfileService} in order to operate with this class.
	 *
	 * @return the service class
	 */
	protected abstract Class<? extends BleProfileService> getServiceClass();

	/**
	 * Returns the service interface that may be used to communicate with the sensor. This will return <code>null</code> if the device is disconnected from the
	 * sensor.
	 *
	 * @return the service binder or <code>null</code>
	 */
	protected E getService() {
		return mService;
	}

	/**
	 * You may do some initialization here. This method is called from {@link #onCreate(Bundle)} before the view was created.
	 */
	protected void onInitialize(final Bundle savedInstanceState) {
		// empty default implementation
	}

	/**
	 * Called from {@link #onCreate(Bundle)}. This method should build the activity UI, f.e. using {@link #setContentView(int)}. Use to obtain references to
	 * views. Connect/Disconnect button, the device name view and battery level view are manager automatically.
	 *
	 * @param savedInstanceState contains the data it most recently supplied in {@link #onSaveInstanceState(Bundle)}. Note: <b>Otherwise it is null</b>.
	 */
	protected abstract void onCreateView(final Bundle savedInstanceState);

	/**
	 * Called after the view has been created.
	 *
	 * @param savedInstanceState contains the data it most recently supplied in {@link #onSaveInstanceState(Bundle)}. Note: <b>Otherwise it is null</b>.
	 */
	protected void onViewCreated(final Bundle savedInstanceState) {
		// empty default implementation
	}

	/**
	 * Called after the view and the toolbar has been created.
	 */
	protected final void setUpView() {
		// set GUI
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mConnectButton = findViewById(R.id.action_connect);
		mDeviceNameView = findViewById(R.id.device_name);
		mBatteryLevelView = findViewById(R.id.battery);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SIS_DEVICE_NAME, mDeviceName);
		outState.putParcelable(SIS_DEVICE, mBluetoothDevice);
		if (mLogSession != null)
			outState.putParcelable(LOG_URI, mLogSession.getSessionUri());
	}

	@Override
	protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mDeviceName = savedInstanceState.getString(SIS_DEVICE_NAME);
		mBluetoothDevice = savedInstanceState.getParcelable(SIS_DEVICE);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

	/**
	 * Use this method to handle menu actions other than home and about.
	 *
	 * @param itemId the menu item id
	 * @return <code>true</code> if action has been handled
	 */
	protected boolean onOptionsItemSelected(final int itemId) {
		// Overwrite when using menu other than R.menu.help
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		final int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				onBackPressed();
				break;
			case R.id.action_about:
				final AppHelpFragment fragment = AppHelpFragment.getInstance(getAboutTextId());
				fragment.show(getSupportFragmentManager(), "help_fragment");
				break;
			default:
				return onOptionsItemSelected(id);
		}
		return true;
	}

	/**
	 * Called when user press CONNECT or DISCONNECT button. See layout files -> onClick attribute.
	 */
	public void onConnectClicked(final View view) {
		if (isBLEEnabled()) {
			if (mService == null) {
				setDefaultUI();
				showDeviceScanningDialog(getFilterUUID());
			} else {
				mService.disconnect();
			}
		} else {
			showBLEDialog();
		}
	}

	/**
	 * Returns the title resource id that will be used to create logger session. If 0 is returned (default) logger will not be used.
	 *
	 * @return the title resource id
	 */
	protected int getLoggerProfileTitle() {
		return 0;
	}

	/**
	 * This method may return the local log content provider authority if local log sessions are supported.
	 *
	 * @return local log session content provider URI
	 */
	protected Uri getLocalAuthorityLogger() {
		return null;
	}

	@Override
	public void onDeviceSelected(final BluetoothDevice device, final String name) {
		final int titleId = getLoggerProfileTitle();
		if (titleId > 0) {
			mLogSession = Logger.newSession(getApplicationContext(), getString(titleId), device.getAddress(), name);
			// If nRF Logger is not installed we may want to use local logger
			if (mLogSession == null && getLocalAuthorityLogger() != null) {
				mLogSession = LocalLogSession.newSession(getApplicationContext(), getLocalAuthorityLogger(), device.getAddress(), name);
			}
		}
		mBluetoothDevice = device;
		mDeviceName = name;

		// The device may not be in the range but the service will try to connect to it if it reach it
		Logger.d(mLogSession, "Creating service...");
		final Intent service = new Intent(this, getServiceClass());
		service.putExtra(BleProfileService.EXTRA_DEVICE_ADDRESS, device.getAddress());
		service.putExtra(BleProfileService.EXTRA_DEVICE_NAME, name);
		if (mLogSession != null)
			service.putExtra(BleProfileService.EXTRA_LOG_URI, mLogSession.getSessionUri());
		startService(service);
		Logger.d(mLogSession, "Binding to the service...");
		bindService(service, mServiceConnection, 0);
	}

	@Override
	public void onDialogCanceled() {
		// do nothing
	}

	@Override
	public void onDeviceConnecting(final BluetoothDevice device) {
		mDeviceNameView.setText(mDeviceName != null ? mDeviceName : getString(R.string.not_available));
		mConnectButton.setText(R.string.action_connecting);
	}

	@Override
	public void onDeviceConnected(final BluetoothDevice device) {
		mDeviceNameView.setText(mDeviceName);
		mConnectButton.setText(R.string.action_disconnect);
	}

	@Override
	public void onDeviceDisconnecting(final BluetoothDevice device) {
		mConnectButton.setText(R.string.action_disconnecting);
	}

	@Override
	public void onDeviceDisconnected(final BluetoothDevice device) {
		mConnectButton.setText(R.string.action_connect);
		mDeviceNameView.setText(getDefaultDeviceName());
		if (mBatteryLevelView != null)
			mBatteryLevelView.setText(R.string.not_available);

		try {
			Logger.d(mLogSession, "Unbinding from the service...");
			unbindService(mServiceConnection);
			mService = null;

			Logger.d(mLogSession, "Activity unbound from the service");
			onServiceUnbinded();
			mDeviceName = null;
			mBluetoothDevice = null;
			mLogSession = null;
		} catch (final IllegalArgumentException e) {
			// do nothing. This should never happen but does...
		}
	}

	@Override
	public void onLinklossOccur(final BluetoothDevice device) {
		if (mBatteryLevelView != null)
			mBatteryLevelView.setText(R.string.not_available);
	}

	@Override
	public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
		// empty default implementation
	}

	@Override
	public void onDeviceReady(final BluetoothDevice device) {
		// empty default implementation
	}

	@Override
	public void onBondingRequired(final BluetoothDevice device) {
		// empty default implementation
	}

	@Override
	public void onBonded(final BluetoothDevice device) {
		// empty default implementation
	}

	@Override
	public final boolean shouldEnableBatteryLevelNotifications(final BluetoothDevice device) {
		// This method will never be called.
		// Please see BleProfileService#shouldEnableBatteryLevelNotifications(BluetoothDevice) instead.
		throw new UnsupportedOperationException("This method should not be called");
	}

	@Override
	public void onBatteryValueReceived(final BluetoothDevice device, final int value) {
		if (mBatteryLevelView != null)
			mBatteryLevelView.setText(getString(R.string.battery, value));
	}

	@Override
	public void onError(final BluetoothDevice device, final String message, final int errorCode) {
		DebugLogger.e(TAG, "Error occurred: " + message + ",  error code: " + errorCode);
		showToast(message + " (" + errorCode + ")");
	}

	@Override
	public void onDeviceNotSupported(final BluetoothDevice device) {
		showToast(R.string.not_supported);
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 *
	 * @param message a message to be shown
	 */
	protected void showToast(final String message) {
		runOnUiThread(() -> Toast.makeText(BleProfileServiceReadyActivity.this, message, Toast.LENGTH_LONG).show());
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 *
	 * @param messageResId an resource id of the message to be shown
	 */
	protected void showToast(final int messageResId) {
		runOnUiThread(() -> Toast.makeText(BleProfileServiceReadyActivity.this, messageResId, Toast.LENGTH_SHORT).show());
	}

	/**
	 * Returns <code>true</code> if the device is connected. Services may not have been discovered yet.
	 */
	protected boolean isDeviceConnected() {
		return mService != null && mService.isConnected();
	}

	/**
	 * Returns the name of the device that the phone is currently connected to or was connected last time
	 */
	protected String getDeviceName() {
		return mDeviceName;
	}

	/**
	 * Restores the default UI before reconnecting
	 */
	protected abstract void setDefaultUI();

	/**
	 * Returns the default device name resource id. The real device name is obtained when connecting to the device. This one is used when device has
	 * disconnected.
	 *
	 * @return the default device name resource id
	 */
	protected abstract int getDefaultDeviceName();

	/**
	 * Returns the string resource id that will be shown in About box
	 *
	 * @return the about resource id
	 */
	protected abstract int getAboutTextId();

	/**
	 * The UUID filter is used to filter out available devices that does not have such UUID in their advertisement packet. See also:
	 * {@link #isChangingConfigurations()}.
	 *
	 * @return the required UUID or <code>null</code>
	 */
	protected abstract UUID getFilterUUID();

	/**
	 * Checks the {@link BleProfileService#EXTRA_DEVICE} in the given intent and compares it with the connected BluetoothDevice object.
	 * @param intent intent received via a broadcast from the service
	 * @return true if the data in the intent apply to the connected device, false otherwise
	 */
	protected boolean isBroadcastForThisDevice(final Intent intent) {
		final BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BleProfileService.EXTRA_DEVICE);
		return mBluetoothDevice != null && mBluetoothDevice.equals(bluetoothDevice);
	}

	/**
	 * Shows the scanner fragment.
	 *
	 * @param filter               the UUID filter used to filter out available devices. The fragment will always show all bonded devices as there is no information about their
	 *                             services
	 * @see #getFilterUUID()
	 */
	private void showDeviceScanningDialog(final UUID filter) {
		final ScannerFragment dialog = ScannerFragment.getInstance(filter);
		dialog.show(getSupportFragmentManager(), "scan_fragment");
	}

	/**
	 * Returns the log session. Log session is created when the device was selected using the {@link ScannerFragment} and released when user press DISCONNECT.
	 *
	 * @return the logger session or <code>null</code>
	 */
	protected ILogSession getLogSession() {
		return mLogSession;
	}

	private void ensureBLESupported() {
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
			finish();
		}
	}

	protected boolean isBLEEnabled() {
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		final BluetoothAdapter adapter = bluetoothManager.getAdapter();
		return adapter != null && adapter.isEnabled();
	}

	protected void showBLEDialog() {
		final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	}
}
