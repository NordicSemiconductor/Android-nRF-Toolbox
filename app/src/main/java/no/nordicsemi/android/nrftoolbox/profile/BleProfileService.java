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

import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.R;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public abstract class BleProfileService extends Service implements BleManagerCallbacks {
	@SuppressWarnings("unused")
	private static final String TAG = "BleProfileService";

	public static final String BROADCAST_CONNECTION_STATE = "no.nordicsemi.android.nrftoolbox.BROADCAST_CONNECTION_STATE";
	public static final String BROADCAST_SERVICES_DISCOVERED = "no.nordicsemi.android.nrftoolbox.BROADCAST_SERVICES_DISCOVERED";
	public static final String BROADCAST_BOND_STATE = "no.nordicsemi.android.nrftoolbox.BROADCAST_BOND_STATE";
	public static final String BROADCAST_BATTERY_LEVEL = "no.nordicsemi.android.nrftoolbox.BROADCAST_BATTERY_LEVEL";
	public static final String BROADCAST_ERROR = "no.nordicsemi.android.nrftoolbox.BROADCAST_ERROR";

	/** The parameter passed when creating the service. Must contain the address of the sensor that we want to connect to */
	public static final String EXTRA_DEVICE_ADDRESS = "no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE_ADDRESS";
	/** The key for the device name that is returned in {@link #BROADCAST_CONNECTION_STATE} with state {@link #STATE_CONNECTED}. */
	public static final String EXTRA_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.EXTRA_DEVICE_NAME";
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

	private boolean mConnected;
	private String mDeviceAddress;
	private String mDeviceName;
	private ILogSession mLogSession;

	public class LocalBinder extends Binder {
		/**
		 * Disconnects from the sensor.
		 */
		public final void disconnect() {
			if (!mConnected) {
				onDeviceDisconnected();
				return;
			}

			// notify user about changing the state to DISCONNECTING
			final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
			broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTING);
			LocalBroadcastManager.getInstance(BleProfileService.this).sendBroadcast(broadcast);

			mBleManager.disconnect();
		}

		/**
		 * Returns the device address
		 * 
		 * @return device address
		 */
		public String getDeviceAddress() {
			return mDeviceAddress;
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
		 * Returns <code>true</code> if the device is connected to the sensor.
		 * 
		 * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
		 */
		public boolean isConnected() {
			return mConnected;
		}

		/**
		 * Returns the log session that can be used to append log entries. The log session is created when the service is being created. The method returns <code>null</code> if the nRF Logger app was
		 * not installed.
		 * 
		 * @return the log session
		 */
		protected ILogSession getLogSession() {
			return mLogSession;
		}
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return getBinder();
	}

	/**
	 * Returns the binder implementation. This must return class implementing the additional manager interface that may be used in the binded activity.
	 * 
	 * @return the service binder
	 */
	protected LocalBinder getBinder() {
		// default implementation returns the basic binder. You can overwrite the LocalBinder with your own, wider implementation
		return new LocalBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// we must allow to rebind to the same service
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate() {
		super.onCreate();

		mHandler = new Handler();

		// initialize the manager
		mBleManager = initializeManager();
		mBleManager.setGattCallbacks(this);
	}

	@SuppressWarnings("rawtypes")
	protected abstract BleManager initializeManager();

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		if (intent == null || !intent.hasExtra(EXTRA_DEVICE_ADDRESS))
			throw new UnsupportedOperationException("No device address at EXTRA_DEVICE_ADDRESS key");

		final Uri logUri = intent.getParcelableExtra(EXTRA_LOG_URI);
		mLogSession = Logger.openSession(getApplicationContext(), logUri);
		mDeviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);

		Logger.i(mLogSession, "Service started");

		// notify user about changing the state to CONNECTING
		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTING);
		LocalBroadcastManager.getInstance(BleProfileService.this).sendBroadcast(broadcast);

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		final BluetoothAdapter adapter = bluetoothManager.getAdapter();
		final BluetoothDevice device = adapter.getRemoteDevice(mDeviceAddress);
		mDeviceName = device.getName();
		onServiceStarted();

		Logger.v(mLogSession, "Connecting...");
		mBleManager.connect(BleProfileService.this, device);
		return START_REDELIVER_INTENT;
	}

	/**
	 * Called when the service has been started. The device name and address are set. It nRF Logger is installed than logger was also initialized.
	 */
	protected void onServiceStarted() {
		// empty default implementation
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// shutdown the manager
		mBleManager.closeBluetoothGatt();
		Logger.i(mLogSession, "Service destroyed");
		mBleManager = null;
		mDeviceAddress = null;
		mDeviceName = null;
		mConnected = false;
		mLogSession = null;
	}

	@Override
	public void onDeviceConnected() {
		Logger.i(mLogSession, "Connected to " + mDeviceAddress);
		mConnected = true;

		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTED);
		broadcast.putExtra(EXTRA_DEVICE_ADDRESS, mDeviceAddress);
		broadcast.putExtra(EXTRA_DEVICE_NAME, mDeviceName);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceDisconnected() {
		Logger.i(mLogSession, "Disconnected");
		mConnected = false;
		mDeviceAddress = null;
		mDeviceName = null;

		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

		// user requested disconnection. We must stop the service
		Logger.v(mLogSession, "Stopping service...");
		stopSelf();
	}

	@Override
	public void onLinklossOccur() {
		Logger.w(mLogSession, "Connection lost");
		mConnected = false;

		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_LINK_LOSS);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onServicesDiscovered(final boolean optionalServicesFound) {
		Logger.i(mLogSession, "Services Discovered");
		Logger.v(mLogSession, "Primary service found");
		if (optionalServicesFound)
			Logger.v(mLogSession, "Secondary service found");

		final Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
		broadcast.putExtra(EXTRA_SERVICE_PRIMARY, true);
		broadcast.putExtra(EXTRA_SERVICE_SECONDARY, optionalServicesFound);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceNotSupported() {
		Logger.i(mLogSession, "Services Discovered");
		Logger.w(mLogSession, "Device is not supported");

		final Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
		broadcast.putExtra(EXTRA_SERVICE_PRIMARY, false);
		broadcast.putExtra(EXTRA_SERVICE_SECONDARY, false);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

		// no need for disconnecting, it will be disconnected by the manager automatically
	}

	@Override
	public void onBatteryValueReceived(final int value) {
		Logger.i(mLogSession, "Battery level received: " + value + "%");

		final Intent broadcast = new Intent(BROADCAST_BATTERY_LEVEL);
		broadcast.putExtra(EXTRA_BATTERY_LEVEL, value);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onBondingRequired() {
		Logger.v(mLogSession, "Bond state: Bonding...");
		showToast(R.string.bonding);

		final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
		broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDING);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onBonded() {
		Logger.i(mLogSession, "Bond state: Bonded");
		showToast(R.string.bonded);

		final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
		broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onError(final String message, final int errorCode) {
		Logger.e(mLogSession, message + " (" + errorCode + ")");

		final Intent broadcast = new Intent(BROADCAST_ERROR);
		broadcast.putExtra(EXTRA_ERROR_MESSAGE, message);
		broadcast.putExtra(EXTRA_ERROR_CODE, errorCode);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

		mBleManager.disconnect();
		stopSelf();
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 * 
	 * @param messageResId
	 *            an resource id of the message to be shown
	 */
	protected void showToast(final int messageResId) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(BleProfileService.this, messageResId, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 * 
	 * @param message
	 *            a message to be shown
	 */
	protected void showToast(final String message) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(BleProfileService.this, message, Toast.LENGTH_SHORT).show();
			}
		});
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
		return mDeviceAddress;
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
		return mConnected;
	}
}
