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
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
@SuppressWarnings("unused")
public abstract class BleProfileServiceReadyActivity<E extends BleProfileService.LocalBinder> extends AppCompatActivity implements
		ScannerFragment.OnDeviceSelectedListener, BleManagerCallbacks {
	private static final String TAG = "BleProfileServiceReadyActivity";

	private static final String SIS_DEVICE_NAME = "device_name";
	private static final String SIS_DEVICE = "device";
	private static final String LOG_URI = "log_uri";
	protected static final int REQUEST_ENABLE_BT = 2;

	private E service;

	private TextView deviceNameView;
	private Button connectButton;

	private ILogSession logSession;
	private BluetoothDevice bluetoothDevice;
	private String deviceName;

	private final BroadcastReceiver commonBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			// Check if the broadcast applies the connected device
			if (!isBroadcastForThisDevice(intent))
				return;

			final BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BleProfileService.EXTRA_DEVICE);
			if (bluetoothDevice == null)
				return;

			final String action = intent.getAction();
			switch (action) {
				case BleProfileService.BROADCAST_CONNECTION_STATE: {
					final int state = intent.getIntExtra(BleProfileService.EXTRA_CONNECTION_STATE, BleProfileService.STATE_DISCONNECTED);

					switch (state) {
						case BleProfileService.STATE_CONNECTED: {
							deviceName = intent.getStringExtra(BleProfileService.EXTRA_DEVICE_NAME);
							onDeviceConnected(bluetoothDevice);
							break;
						}
						case BleProfileService.STATE_DISCONNECTED: {
							onDeviceDisconnected(bluetoothDevice);
							deviceName = null;
							break;
						}
						case BleProfileService.STATE_LINK_LOSS: {
							onLinkLossOccurred(bluetoothDevice);
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
				case BleProfileService.BROADCAST_ERROR: {
					final String message = intent.getStringExtra(BleProfileService.EXTRA_ERROR_MESSAGE);
					final int errorCode = intent.getIntExtra(BleProfileService.EXTRA_ERROR_CODE, 0);
					onError(bluetoothDevice, message, errorCode);
					break;
				}
			}
		}
	};

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(final ComponentName name, final IBinder service) {
			final E bleService = BleProfileServiceReadyActivity.this.service = (E) service;
			bluetoothDevice = bleService.getBluetoothDevice();
			logSession = bleService.getLogSession();
			Logger.d(logSession, "Activity bound to the service");
			onServiceBound(bleService);

			// Update UI
			deviceName = bleService.getDeviceName();
			deviceNameView.setText(deviceName);
			connectButton.setText(R.string.action_disconnect);

			// And notify user if device is connected
			if (bleService.isConnected()) {
				onDeviceConnected(bluetoothDevice);
			} else {
				// If the device is not connected it means that either it is still connecting,
				// or the link was lost and service is trying to connect to it (autoConnect=true).
				onDeviceConnecting(bluetoothDevice);
			}
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			// Note: this method is called only when the service is killed by the system,
			// not when it stops itself or is stopped by the activity.
			// It will be called only when there is critically low memory, in practice never
			// when the activity is in foreground.
			Logger.d(logSession, "Activity disconnected from the service");
			deviceNameView.setText(getDefaultDeviceName());
			connectButton.setText(R.string.action_connect);

			service = null;
			deviceName = null;
			bluetoothDevice = null;
			logSession = null;
			onServiceUnbound();
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
			logSession = Logger.openSession(getApplicationContext(), logUri);
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

		LocalBroadcastManager.getInstance(this).registerReceiver(commonBroadcastReceiver, makeIntentFilter());
	}

	@Override
	protected void onStart() {
		super.onStart();

		/*
		 * If the service has not been started before, the following lines will not start it.
		 * However, if it's running, the Activity will bind to it and notified via serviceConnection.
		 */
		final Intent service = new Intent(this, getServiceClass());
		// We pass 0 as a flag so the service will not be created if not exists.
		bindService(service, serviceConnection, 0);

		/*
		 * When user exited the UARTActivity while being connected, the log session is kept in
		 * the service. We may not get it before binding to it so in this case this event will
		 * not be logged (logSession is null until onServiceConnected(..) is called).
		 * It will, however, be logged after the orientation changes.
		 */
	}

	@Override
	protected void onStop() {
		super.onStop();

		try {
			// We don't want to perform some operations (e.g. disable Battery Level notifications)
			// in the service if we are just rotating the screen. However, when the activity will
			// disappear, we may want to disable some device features to reduce the battery
			// consumption.
			if (service != null)
				service.setActivityIsChangingConfiguration(isChangingConfigurations());

			unbindService(serviceConnection);
			service = null;

			Logger.d(logSession, "Activity unbound from the service");
			onServiceUnbound();
			deviceName = null;
			bluetoothDevice = null;
			logSession = null;
		} catch (final IllegalArgumentException e) {
			// do nothing, we were not connected to the sensor
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(commonBroadcastReceiver);
	}

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BleProfileService.BROADCAST_CONNECTION_STATE);
		intentFilter.addAction(BleProfileService.BROADCAST_SERVICES_DISCOVERED);
		intentFilter.addAction(BleProfileService.BROADCAST_DEVICE_READY);
		intentFilter.addAction(BleProfileService.BROADCAST_BOND_STATE);
		intentFilter.addAction(BleProfileService.BROADCAST_ERROR);
		return intentFilter;
	}

	/**
	 * Called when activity binds to the service. The parameter is the object returned in {@link Service#onBind(Intent)} method in your service. The method is
	 * called when device gets connected or is created while sensor was connected before. You may use the binder as a sensor interface.
	 */
	protected abstract void onServiceBound(E binder);

	/**
	 * Called when activity unbinds from the service. You may no longer use this binder because the sensor was disconnected. This method is also called when you
	 * leave the activity being connected to the sensor in the background.
	 */
	protected abstract void onServiceUnbound();

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
		return service;
	}

	/**
	 * You may do some initialization here. This method is called from {@link #onCreate(Bundle)} before the view was created.
	 */
	protected void onInitialize(final Bundle savedInstanceState) {
		// empty default implementation
	}

	/**
	 * Called from {@link #onCreate(Bundle)}. This method should build the activity UI, i.e. using {@link #setContentView(int)}.
	 * Use to obtain references to views. Connect/Disconnect button, the device name view are manager automatically.
	 *
	 * @param savedInstanceState contains the data it most recently supplied in {@link #onSaveInstanceState(Bundle)}.
	 *                           Note: <b>Otherwise it is null</b>.
	 */
	protected abstract void onCreateView(final Bundle savedInstanceState);

	/**
	 * Called after the view has been created.
	 *
	 * @param savedInstanceState contains the data it most recently supplied in {@link #onSaveInstanceState(Bundle)}.
	 *                           Note: <b>Otherwise it is null</b>.
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
		connectButton = findViewById(R.id.action_connect);
		deviceNameView = findViewById(R.id.device_name);
	}

	@Override
	protected void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SIS_DEVICE_NAME, deviceName);
		outState.putParcelable(SIS_DEVICE, bluetoothDevice);
		if (logSession != null)
			outState.putParcelable(LOG_URI, logSession.getSessionUri());
	}

	@Override
	protected void onRestoreInstanceState(final @NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		deviceName = savedInstanceState.getString(SIS_DEVICE_NAME);
		bluetoothDevice = savedInstanceState.getParcelable(SIS_DEVICE);
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
			if (service == null) {
				setDefaultUI();
				showDeviceScanningDialog(getFilterUUID());
			} else {
				service.disconnect();
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
	public void onDeviceSelected(@NonNull final BluetoothDevice device, final String name) {
		final int titleId = getLoggerProfileTitle();
		if (titleId > 0) {
			logSession = Logger.newSession(getApplicationContext(), getString(titleId), device.getAddress(), name);
			// If nRF Logger is not installed we may want to use local logger
			if (logSession == null && getLocalAuthorityLogger() != null) {
				logSession = LocalLogSession.newSession(getApplicationContext(), getLocalAuthorityLogger(), device.getAddress(), name);
			}
		}
		bluetoothDevice = device;
		deviceName = name;

		// The device may not be in the range but the service will try to connect to it if it reach it
		Logger.d(logSession, "Creating service...");
		final Intent service = new Intent(this, getServiceClass());
		service.putExtra(BleProfileService.EXTRA_DEVICE_ADDRESS, device.getAddress());
		service.putExtra(BleProfileService.EXTRA_DEVICE_NAME, name);
		if (logSession != null)
			service.putExtra(BleProfileService.EXTRA_LOG_URI, logSession.getSessionUri());
		startService(service);
		Logger.d(logSession, "Binding to the service...");
		bindService(service, serviceConnection, 0);
	}

	@Override
	public void onDialogCanceled() {
		// do nothing
	}

	@Override
	public void onDeviceConnecting(@NonNull final BluetoothDevice device) {
		deviceNameView.setText(deviceName != null ? deviceName : getString(R.string.not_available));
		connectButton.setText(R.string.action_connecting);
	}

	@Override
	public void onDeviceConnected(@NonNull final BluetoothDevice device) {
		deviceNameView.setText(deviceName);
		connectButton.setText(R.string.action_disconnect);
	}

	@Override
	public void onDeviceDisconnecting(@NonNull final BluetoothDevice device) {
		connectButton.setText(R.string.action_disconnecting);
	}

	@Override
	public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
		connectButton.setText(R.string.action_connect);
		deviceNameView.setText(getDefaultDeviceName());

		try {
			Logger.d(logSession, "Unbinding from the service...");
			unbindService(serviceConnection);
			service = null;

			Logger.d(logSession, "Activity unbound from the service");
			onServiceUnbound();
			deviceName = null;
			bluetoothDevice = null;
			logSession = null;
		} catch (final IllegalArgumentException e) {
			// do nothing. This should never happen but does...
		}
	}

	@Override
	public void onLinkLossOccurred(@NonNull final BluetoothDevice device) {
		// empty default implementation
	}

	@Override
	public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
		// empty default implementation
	}

	@Override
	public void onDeviceReady(@NonNull final BluetoothDevice device) {
		// empty default implementation
	}

	@Override
	public void onBondingRequired(@NonNull final BluetoothDevice device) {
		// empty default implementation
	}

	@Override
	public void onBonded(@NonNull final BluetoothDevice device) {
		// empty default implementation
	}

	@Override
	public void onBondingFailed(@NonNull final BluetoothDevice device) {
		// empty default implementation
	}

	@Override
	public void onError(@NonNull final BluetoothDevice device, @NonNull final String message, final int errorCode) {
		DebugLogger.e(TAG, "Error occurred: " + message + ",  error code: " + errorCode);
		showToast(message + " (" + errorCode + ")");
	}

	@Override
	public void onDeviceNotSupported(@NonNull final BluetoothDevice device) {
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
		return service != null && service.isConnected();
	}

	/**
	 * Returns the name of the device that the phone is currently connected to or was connected last time
	 */
	protected String getDeviceName() {
		return deviceName;
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
		return bluetoothDevice != null && bluetoothDevice.equals(bluetoothDevice);
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
		return logSession;
	}

	private void ensureBLESupported() {
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
			finish();
		}
	}

	protected boolean isBLEEnabled() {
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		return adapter != null && adapter.isEnabled();
	}

	protected void showBLEDialog() {
		final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	}
}
