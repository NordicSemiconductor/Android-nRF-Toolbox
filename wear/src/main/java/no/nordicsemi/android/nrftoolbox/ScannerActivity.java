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

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.Toast;

import no.nordicsemi.android.nrftoolbox.ble.BleProfileService;
import no.nordicsemi.android.nrftoolbox.uart.UARTConfigurationsActivity;

public class ScannerActivity extends Activity {
	private static final String TAG = "ScannerActivity";

	private static final int PERMISSION_REQUEST_LOCATION = 1;

	private DevicesAdapter deviceAdapter;
	private View header;

	private BroadcastReceiver serviceBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();

			switch (action) {
				case BleProfileService.BROADCAST_CONNECTION_STATE: {
					final int state = intent.getIntExtra(BleProfileService.EXTRA_CONNECTION_STATE, BleProfileService.STATE_DISCONNECTED);
					if (state == BleProfileService.STATE_DISCONNECTED)
						deviceAdapter.setConnectingPosition(-1);
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
					deviceAdapter.setConnectingPosition(-1);
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
					deviceAdapter.notifyDataSetChanged(); // TODO check this. Bonding was never tested.
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
		final WearableListView listView = findViewById(R.id.devices_list);
		listView.setAdapter(deviceAdapter = new DevicesAdapter(listView));
		listView.setClickListener(onRowClickListener);
		listView.addOnScrollListener(onScrollListener);

		// The header will be moved as the list is scrolled
		header = findViewById(R.id.header);

		// Register a broadcast receiver that will listen for events from the service.
		LocalBroadcastManager.getInstance(this).registerReceiver(serviceBroadcastReceiver, BleProfileService.makeIntentFilter());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceBroadcastReceiver);
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_LOCATION:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					deviceAdapter.startLeScan();
				} else {
					Toast.makeText(ScannerActivity.this, "Location permission required", Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
				return;
			}
		}
		deviceAdapter.startLeScan();
	}

	@Override
	protected void onPause() {
		super.onPause();
		deviceAdapter.stopLeScan();
	}

	/** List click listener. */
	private WearableListView.ClickListener onRowClickListener = new WearableListView.ClickListener() {
		@Override
		public void onClick(final WearableListView.ViewHolder holder) {
			final DevicesAdapter.ItemViewHolder viewHolder = (DevicesAdapter.ItemViewHolder) holder;
			final BluetoothDevice device = viewHolder.getDevice();

			if (device != null) {
				deviceAdapter.stopLeScan();
				deviceAdapter.setConnectingPosition(holder.getAdapterPosition());

				// Start the service that will connect to selected device
				final Intent service = new Intent(ScannerActivity.this, BleProfileService.class);
				service.putExtra(BleProfileService.EXTRA_DEVICE_ADDRESS, device.getAddress());
				startService(service);
			} else {
				deviceAdapter.startLeScan();
			}
		}

		@Override
		public void onTopEmptyRegionClick() {
			// do nothing
		}
	};

	/** The following code ensures that the title scrolls as the user scrolls up or down the list/ */
	private WearableListView.OnScrollListener onScrollListener = new WearableListView.OnScrollListener() {
		@Override
		public void onAbsoluteScrollChange(final int i) {
			if (i > 0)
				header.setY(-i);
			else
				header.setY(0);
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
