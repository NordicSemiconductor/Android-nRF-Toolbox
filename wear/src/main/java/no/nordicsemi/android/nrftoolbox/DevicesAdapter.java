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

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class DevicesAdapter extends WearableListView.Adapter {
	private static final String TAG = "DevicesAdapter";

	private final static long SCAN_DURATION = 5000;

	private final List<BluetoothDevice> mDevices = new ArrayList<>();
	private final LayoutInflater mInflater;
	private final Handler mHandler;
	private final WearableListView mListView;
	private final String mNotAvailable;
	private final String mConnectingText;
	private final String mAvailableText;
	private final String mBondedText;
	private final String mBondingText;
	/** A position of a device that the activity is currently connecting to. */
	private int mConnectingPosition = -1;
	/** Flag set to true when scanner is active. */
	private boolean mScanning;

	public DevicesAdapter(final WearableListView listView) {
		final Context context = listView.getContext();
		mInflater = LayoutInflater.from(context);
		mNotAvailable = context.getString(R.string.not_available);
		mConnectingText = context.getString(R.string.state_connecting);
		mAvailableText = context.getString(R.string.devices_list_available);
		mBondedText = context.getString(R.string.devices_list_bonded);
		mBondingText = context.getString(R.string.devices_list_bonding);
		mListView = listView;
		mHandler = new Handler();

		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mDevices.addAll(bluetoothAdapter.getBondedDevices());
	}

	@Override
	public WearableListView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
		return new ItemViewHolder(mInflater.inflate(R.layout.device_item, viewGroup, false));
	}

	@Override
	public void onBindViewHolder(final WearableListView.ViewHolder holder, final int position) {
		final ItemViewHolder viewHolder = (ItemViewHolder) holder;

		if (position < mDevices.size()) {
			final BluetoothDevice device = mDevices.get(position);

			viewHolder.mDevice = device;
			viewHolder.mName.setText(TextUtils.isEmpty(device.getName()) ? mNotAvailable : device.getName());
			viewHolder.mAddress.setText(getState(device, position));
			viewHolder.mIcon.showIndeterminateProgress(position == mConnectingPosition);
		} else {
			viewHolder.mDevice = null;
			viewHolder.mName.setText(mScanning ? R.string.devices_list_scanning : R.string.devices_list_start_scan);
			viewHolder.mAddress.setText(null);
			viewHolder.mIcon.showIndeterminateProgress(mScanning);
		}
	}

	@Override
	public int getItemCount() {
		return mDevices.size() + (mConnectingPosition == -1 ? 1 : 0);
	}

	public void setConnectingPosition(final int connectingPosition) {
		final int oldPosition = mConnectingPosition;
		this.mConnectingPosition = connectingPosition;
		if (connectingPosition >= 0) {
			// The "Scan for nearby device' item is removed
			notifyItemChanged(connectingPosition);
			notifyItemRemoved(mDevices.size());
		} else {
			if (oldPosition >= 0)
				notifyItemChanged(oldPosition);
			notifyItemInserted(mDevices.size());
		}
	}

	public void startLeScan() {
		// Scanning is disabled when we are connecting or connected.
		if (mConnectingPosition >= 0)
			return;

		if (mScanning) {
			// Extend scanning for some time more
			mHandler.removeCallbacks(mStopScanTask);
			mHandler.postDelayed(mStopScanTask, SCAN_DURATION);
			return;
		}

		final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
		final ScanSettings settings = new ScanSettings.Builder().setReportDelay(1000).setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
		scanner.startScan(null, settings, mScanCallback);

		// Setup timer that will stop scanning
		mHandler.postDelayed(mStopScanTask, SCAN_DURATION);
		mScanning = true;
		notifyItemChanged(mDevices.size());
	}

	public void stopLeScan() {
		if (!mScanning)
			return;

		final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
		scanner.stopScan(mScanCallback);

		mHandler.removeCallbacks(mStopScanTask);
		mScanning = false;
		notifyItemChanged(mDevices.size());
	}

	private String getState(final BluetoothDevice device, final int position) {
		if (mConnectingPosition == position)
			return mConnectingText;
		else if (device.getBondState() == BluetoothDevice.BOND_BONDED)
			return mBondedText;
		else if (device.getBondState() == BluetoothDevice.BOND_BONDING)
			return mBondingText;
		return mAvailableText;
	}

	private Runnable mStopScanTask = new Runnable() {
		@Override
		public void run() {
			stopLeScan();
		}
	};

	private ScanCallback mScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(final int callbackType, final ScanResult result) {
			// empty
		}

		@Override
		public void onBatchScanResults(final List<ScanResult> results) {
			final int size = mDevices.size();
			for (final ScanResult result : results) {
				final BluetoothDevice device = result.getDevice();
				if (!mDevices.contains(device))
					mDevices.add(device);
			}
			if (size != mDevices.size()) {
				notifyItemRangeInserted(size, mDevices.size() - size);
				if (size == 0)
					mListView.scrollToPosition(0);
			}
		}

		@Override
		public void onScanFailed(final int errorCode) {
			// empty
		}
	};

	public static class ItemViewHolder extends WearableListView.ViewHolder {
		private CircledImageView mIcon;
		private TextView mName;
		private TextView mAddress;
		private BluetoothDevice mDevice;

		public ItemViewHolder(final View itemView) {
			super(itemView);

			mIcon = (CircledImageView) itemView.findViewById(R.id.icon);
			mName = (TextView) itemView.findViewById(R.id.name);
			mAddress = (TextView) itemView.findViewById(R.id.state);
		}

		/** Returns the Bluetooth device for that holder, or null for "Scanning for nearby devices" row. */
		public BluetoothDevice getDevice() {
			return mDevice;
		}
	}
}
