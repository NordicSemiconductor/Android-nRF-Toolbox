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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class DevicesAdapter extends WearableListView.Adapter {
	private static final String TAG = "DevicesAdapter";

	private final static long SCAN_DURATION = 5000;

	private final List<BluetoothDevice> devices = new ArrayList<>();
	private final LayoutInflater inflater;
	private final Handler handler;
	private final WearableListView listView;
	private final String notAvailable;
	private final String connectingText;
	private final String availableText;
	private final String bondedText;
	private final String bondingText;
	/** A position of a device that the activity is currently connecting to. */
	private int connectingPosition = -1;
	/** Flag set to true when scanner is active. */
	private boolean scanning;

	public DevicesAdapter(final WearableListView listView) {
		final Context context = listView.getContext();
		inflater = LayoutInflater.from(context);
		notAvailable = context.getString(R.string.not_available);
		connectingText = context.getString(R.string.state_connecting);
		availableText = context.getString(R.string.devices_list_available);
		bondedText = context.getString(R.string.devices_list_bonded);
		bondingText = context.getString(R.string.devices_list_bonding);
		this.listView = listView;
		handler = new Handler();

		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter != null)
			devices.addAll(bluetoothAdapter.getBondedDevices());
	}

	@NonNull
	@Override
	public WearableListView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int position) {
		return new ItemViewHolder(inflater.inflate(R.layout.device_item, viewGroup, false));
	}

	@Override
	public void onBindViewHolder(@NonNull final WearableListView.ViewHolder holder, final int position) {
		final ItemViewHolder viewHolder = (ItemViewHolder) holder;

		if (position < devices.size()) {
			final BluetoothDevice device = devices.get(position);

			viewHolder.device = device;
			viewHolder.name.setText(TextUtils.isEmpty(device.getName()) ? notAvailable : device.getName());
			viewHolder.address.setText(getState(device, position));
			viewHolder.icon.showIndeterminateProgress(position == connectingPosition);
		} else {
			viewHolder.device = null;
			viewHolder.name.setText(scanning ? R.string.devices_list_scanning : R.string.devices_list_start_scan);
			viewHolder.address.setText(null);
			viewHolder.icon.showIndeterminateProgress(scanning);
		}
	}

	@Override
	public int getItemCount() {
		return devices.size() + (connectingPosition == -1 ? 1 : 0);
	}

	public void setConnectingPosition(final int connectingPosition) {
		final int oldPosition = connectingPosition;
		this.connectingPosition = connectingPosition;
		if (connectingPosition >= 0) {
			// The "Scan for nearby device' item is removed
			notifyItemChanged(connectingPosition);
			notifyItemRemoved(devices.size());
		} else {
			if (oldPosition >= 0)
				notifyItemChanged(oldPosition);
			notifyItemInserted(devices.size());
		}
	}

	public void startLeScan() {
		// Scanning is disabled when we are connecting or connected.
		if (connectingPosition >= 0)
			return;

		if (scanning) {
			// Extend scanning for some time more
			handler.removeCallbacks(stopScanTask);
			handler.postDelayed(stopScanTask, SCAN_DURATION);
			return;
		}

		final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
		final ScanSettings settings = new ScanSettings.Builder().setReportDelay(1000).setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
		scanner.startScan(null, settings, scanCallback);

		// Setup timer that will stop scanning
		handler.postDelayed(stopScanTask, SCAN_DURATION);
		scanning = true;
		notifyItemChanged(devices.size());
	}

	public void stopLeScan() {
		if (!scanning)
			return;

		final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
		scanner.stopScan(scanCallback);

		handler.removeCallbacks(stopScanTask);
		scanning = false;
		notifyItemChanged(devices.size());
	}

	private String getState(final BluetoothDevice device, final int position) {
		if (connectingPosition == position)
			return connectingText;
		else if (device.getBondState() == BluetoothDevice.BOND_BONDED)
			return bondedText;
		else if (device.getBondState() == BluetoothDevice.BOND_BONDING)
			return bondingText;
		return availableText;
	}

	private Runnable stopScanTask = this::stopLeScan;

	private ScanCallback scanCallback = new ScanCallback() {
		@Override
		public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
			// empty
		}

		@Override
		public void onBatchScanResults(final List<ScanResult> results) {
			final int size = devices.size();
			for (final ScanResult result : results) {
				final BluetoothDevice device = result.getDevice();
				if (!devices.contains(device))
					devices.add(device);
			}
			if (size != devices.size()) {
				notifyItemRangeInserted(size, devices.size() - size);
				if (size == 0)
					listView.scrollToPosition(0);
			}
		}

		@Override
		public void onScanFailed(final int errorCode) {
			// empty
		}
	};

	public static class ItemViewHolder extends WearableListView.ViewHolder {
		private CircledImageView icon;
		private TextView name;
		private TextView address;
		private BluetoothDevice device;

		public ItemViewHolder(final View itemView) {
			super(itemView);

			icon = itemView.findViewById(R.id.icon);
			name = itemView.findViewById(R.id.name);
			address = itemView.findViewById(R.id.state);
		}

		/** Returns the Bluetooth device for that holder, or null for "Scanning for nearby devices" row. */
		public BluetoothDevice getDevice() {
			return device;
		}
	}
}
