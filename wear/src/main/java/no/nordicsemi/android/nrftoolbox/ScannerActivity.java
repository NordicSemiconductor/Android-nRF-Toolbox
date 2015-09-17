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

package no.nordicsemi.android.nrftoolbox;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import no.nordicsemi.android.nrftoolbox.ble.BleProfileService;
import no.nordicsemi.android.nrftoolbox.uart.UARTConfigurationsActivity;

public class ScannerActivity extends Activity {
	private static final String TAG = "ScannerActivity";

	private DevicesAdapter mDeviceAdapter;
	private View mHeader;

	private BroadcastReceiver mServiceBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();

			switch (action) {
				case BleProfileService.BROADCAST_CONNECTION_STATE: {
					final int state = intent.getIntExtra(BleProfileService.EXTRA_CONNECTION_STATE, BleProfileService.STATE_DISCONNECTED);
					if (state == BleProfileService.STATE_DISCONNECTED)
						mDeviceAdapter.setConnectingPosition(-1);
					break;
				}
				case BleProfileService.BROADCAST_DEVICE_READY: {
					final Intent activity = new Intent(ScannerActivity.this, UARTConfigurationsActivity.class);
					startActivity(activity);
					finish();
					break;
				}
				case BleProfileService.BROADCAST_DEVICE_NOT_SUPPORTED: {
					Toast.makeText(ScannerActivity.this, R.string.devices_list_device_not_supported, Toast.LENGTH_SHORT).show();
					mDeviceAdapter.setConnectingPosition(-1);
					break;
				}
				case BleProfileService.BROADCAST_ERROR: {
					final String message = intent.getStringExtra(BleProfileService.EXTRA_ERROR_MESSAGE);
					// final int errorCode = intent.getIntExtra(BleProfileService.EXTRA_ERROR_CODE, 0);
					Toast.makeText(ScannerActivity.this, message, Toast.LENGTH_SHORT).show();
					// TODO error handing
					break;
				}
				case BleProfileService.BROADCAST_BOND_STATE: {
					mDeviceAdapter.notifyDataSetChanged(); // TODO check this. Bonding was never tested.
					break;
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_with_header);

		// Get the list component from the layout of the activity
		final WearableListView listView = (WearableListView) findViewById(R.id.devices_list);
		listView.setAdapter(mDeviceAdapter = new DevicesAdapter(listView));
		listView.setClickListener(mOnRowClickListener);
		listView.addOnScrollListener(mOnScrollListener);

		// The header will be moved as the list is scrolled
		mHeader = findViewById(R.id.header);

		// Register a broadcast receiver that will listen for events from the service.
		LocalBroadcastManager.getInstance(this).registerReceiver(mServiceBroadcastReceiver, BleProfileService.makeIntentFilter());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceBroadcastReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mDeviceAdapter.startLeScan();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mDeviceAdapter.stopLeScan();
	}

	/** List click listener. */
	private WearableListView.ClickListener mOnRowClickListener = new WearableListView.ClickListener() {
		@Override
		public void onClick(final WearableListView.ViewHolder holder) {
			final DevicesAdapter.ItemViewHolder viewHolder = (DevicesAdapter.ItemViewHolder) holder;
			final BluetoothDevice device = viewHolder.getDevice();

			if (device != null) {
				mDeviceAdapter.stopLeScan();
				mDeviceAdapter.setConnectingPosition(holder.getAdapterPosition());

				// Start the service that will connect to selected device
				final Intent service = new Intent(ScannerActivity.this, BleProfileService.class);
				service.putExtra(BleProfileService.EXTRA_DEVICE_ADDRESS, device.getAddress());
				startService(service);
			} else {
				mDeviceAdapter.startLeScan();
			}
		}

		@Override
		public void onTopEmptyRegionClick() {
			// do nothing
		}
	};

	/** The following code ensures that the title scrolls as the user scrolls up or down the list/ */
	private WearableListView.OnScrollListener mOnScrollListener = new WearableListView.OnScrollListener() {
		@Override
		public void onAbsoluteScrollChange(final int i) {
			if (i > 0)
				mHeader.setY(-i);
			else
				mHeader.setY(0);
		}

		@Override
		public void onScroll(final int i) {
			// Placeholder
		}

		@Override
		public void onScrollStateChanged(final int i) {
			// Placeholder
		}

		@Override
		public void onCentralPositionChanged(final int i) {
			// Placeholder
		}
	};
}
