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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.LocalLogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.AppHelpFragment;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerFragment;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public abstract class BleProfileActivity extends AppCompatActivity implements BleManagerCallbacks, ScannerFragment.OnDeviceSelectedListener {
	private static final String TAG = "BaseProfileActivity";

	private static final String CONNECTION_STATUS = "connection_status";
	private static final String DEVICE_NAME = "device_name";
	private static final int REQUEST_ENABLE_BT = 2;

	private BleManager<? extends BleManagerCallbacks> mBleManager;

	private TextView mDeviceNameView;
	private TextView mBatteryLevelView;
	private Button mConnectButton;
	private ILogSession mLogSession;

	private boolean mDeviceConnected = false;
	private String mDeviceName;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ensureBLESupported();
		if (!isBLEEnabled()) {
			showBLEDialog();
		}

		/*
		 * We use the managers using a singleton pattern. It's not recommended for the Android, because the singleton instance remains after Activity has been
		 * destroyed but it's simple and is used only for this demo purpose. In final application Managers should be created as a non-static objects in
		 * Services. The Service should implement ManagerCallbacks interface. The application Activity may communicate with such Service using binding,
		 * broadcast listeners, local broadcast listeners (see support.v4 library), or messages. See the Proximity profile for Service approach.
		 */
		mBleManager = initializeManager();
		onInitialize(savedInstanceState);
		onCreateView(savedInstanceState);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

		onViewCreated(savedInstanceState);
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
	 * @param savedInstanceState
	 *            contains the data it most recently supplied in {@link #onSaveInstanceState(Bundle)}. Note: <b>Otherwise it is null</b>.
	 */
	protected abstract void onCreateView(final Bundle savedInstanceState);

	/**
	 * Called after the view has been created.
	 * 
	 * @param savedInstanceState
	 */
	protected final void onViewCreated(final Bundle savedInstanceState) {
		// set GUI
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mConnectButton = (Button) findViewById(R.id.action_connect);
		mDeviceNameView = (TextView) findViewById(R.id.device_name);
		mBatteryLevelView = (TextView) findViewById(R.id.battery);
	}

	@Override
	public void onBackPressed() {
		mBleManager.disconnect();
		super.onBackPressed();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(CONNECTION_STATUS, mDeviceConnected);
		outState.putString(DEVICE_NAME, mDeviceName);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mDeviceConnected = savedInstanceState.getBoolean(CONNECTION_STATUS);
		mDeviceName = savedInstanceState.getString(DEVICE_NAME);

		if (mDeviceConnected) {
			mConnectButton.setText(R.string.action_disconnect);
		} else {
			mConnectButton.setText(R.string.action_connect);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

	/**
	 * Use this method to handle menu actions other than home and about.
	 * 
	 * @param itemId
	 *            the menu item id
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
			if (!mDeviceConnected) {
				setDefaultUI();
				showDeviceScanningDialog(getFilterUUID());
			} else {
				mBleManager.disconnect();
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
		mDeviceName = name;
		mBleManager.setLogger(mLogSession);
		mDeviceNameView.setText(name != null ? name : getString(R.string.not_available));
		mConnectButton.setText(R.string.action_disconnect);
		mBleManager.connect(device);
	}

	@Override
	public void onDialogCanceled() {
		// do nothing
	}

	@Override
	public void onDeviceConnected() {
		mDeviceConnected = true;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectButton.setText(R.string.action_disconnect);
			}
		});
	}

	@Override
	public void onDeviceDisconnecting() {
		// do nothing
	}

	@Override
	public void onDeviceDisconnected() {
		mDeviceConnected = false;
		mBleManager.close();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectButton.setText(R.string.action_connect);
				mDeviceNameView.setText(getDefaultDeviceName());
				mBatteryLevelView.setText(R.string.not_available);
			}
		});
	}

	@Override
	public void onLinklossOccur() {
		mDeviceConnected = false;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectButton.setText(R.string.action_connect);
				mDeviceNameView.setText(getDefaultDeviceName());
				mBatteryLevelView.setText(R.string.not_available);
			}
		});
	}

	@Override
	public void onBatteryValueReceived(final int value) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBatteryLevelView.setText(getString(R.string.battery, value));
			}
		});
	}

	@Override
	public void onBondingRequired() {
		showToast(R.string.bonding);
	}

	@Override
	public void onBonded() {
		showToast(R.string.bonded);
	}

	@Override
	public void onError(final String message, final int errorCode) {
		DebugLogger.e(TAG, "Error occurred: " + message + ",  error code: " + errorCode);
		showToast(message + " (" + errorCode + ")");
	}

	@Override
	public void onDeviceNotSupported() {
		showToast(R.string.not_supported);
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 * 
	 * @param message
	 *            a message to be shown
	 */
	protected void showToast(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(BleProfileActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 * 
	 * @param messageResId
	 *            an resource id of the message to be shown
	 */
	protected void showToast(final int messageResId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(BleProfileActivity.this, messageResId, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Returns <code>true</code> if the device is connected. Services may not have been discovered yet.
	 */
	protected boolean isDeviceConnected() {
		return mDeviceConnected;
	}

	/**
	 * Returns the name of the device that the phone is currently connected to or was connected last time
	 */
	protected String getDeviceName() {
		return mDeviceName;
	}

	/**
	 * Initializes the Bluetooth Low Energy manager. A manager is used to communicate with profile's services.
	 * 
	 * @return the manager that was created
	 */
	protected abstract BleManager<? extends BleManagerCallbacks> initializeManager();

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
	 * Shows the scanner fragment.
	 * 
	 * @param filter
	 *            the UUID filter used to filter out available devices. The fragment will always show all bonded devices as there is no information about their
	 *            services
	 * @see #getFilterUUID()
	 */
	private void showDeviceScanningDialog(final UUID filter) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final ScannerFragment dialog = ScannerFragment.getInstance(filter);
				dialog.show(getSupportFragmentManager(), "scan_fragment");
			}
		});
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
