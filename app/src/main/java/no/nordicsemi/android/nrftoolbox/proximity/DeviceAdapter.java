/*
 * Copyright (c) 2016, Nordic Semiconductor
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

package no.nordicsemi.android.nrftoolbox.proximity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import no.nordicsemi.android.nrftoolbox.R;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
	private final ProximityService.ProximityBinder mService;
	private final List<BluetoothDevice> mDevices;

	public DeviceAdapter(final ProximityService.ProximityBinder binder) {
		mService = binder;
		mDevices = mService.getManagedDevices();
	}

	@Override
	public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
		final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_feature_proximity_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		holder.bind(mDevices.get(position));
	}

	@Override
	public int getItemCount() {
		return mDevices.size();
	}

	public void onDeviceAdded(final BluetoothDevice device) {
		final int position = mDevices.indexOf(device);
		if (position == -1) {
			notifyItemInserted(mDevices.size() - 1);
		} else {
			// This may happen when Bluetooth adapter was switched off and on again
			// while there were devices on the list.
			notifyItemChanged(position);
		}
	}

	public void onDeviceRemoved(final BluetoothDevice device) {
		notifyDataSetChanged(); // we don't have position of the removed device here
	}

	public void onDeviceStateChanged(final BluetoothDevice device) {
		final int position = mDevices.indexOf(device);
		if (position >= 0)
			notifyItemChanged(position);
	}

	public void onBatteryValueReceived(final BluetoothDevice device) {
		final int position = mDevices.indexOf(device);
		if (position >= 0)
			notifyItemChanged(position);
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private TextView nameView;
		private TextView addressView;
		private TextView batteryView;
		private ImageButton actionButton;

		public ViewHolder(final View itemView) {
			super(itemView);

			nameView = itemView.findViewById(R.id.name);
			addressView = itemView.findViewById(R.id.address);
			batteryView = itemView.findViewById(R.id.battery);
			actionButton = itemView.findViewById(R.id.action_find_silent);

			// Configure FIND / SILENT button
			actionButton.setOnClickListener(v -> {
				final int position = getAdapterPosition();
				final BluetoothDevice device = mDevices.get(position);
				final boolean on = mService.toggleImmediateAlert(device);

				actionButton.setImageResource(on ? R.drawable.ic_stat_notify_proximity_silent : R.drawable.ic_stat_notify_proximity_find);
			});

			// Configure Disconnect button
			itemView.findViewById(R.id.action_disconnect).setOnClickListener(v -> {
				final int position = getAdapterPosition();
				final BluetoothDevice device = mDevices.get(position);
				mService.disconnect(device);
				// The device might have not been connected, so there will be no callback
				onDeviceRemoved(device);
			});
		}

		private void bind(final BluetoothDevice device) {
			final int state = mService.getConnectionState(device);

			String name = device.getName();
			if (TextUtils.isEmpty(name))
				name = nameView.getResources().getString(R.string.proximity_default_device_name);
			nameView.setText(name);
			addressView.setText(device.getAddress());

			final boolean on = mService.isImmediateAlertOn(device);
			actionButton.setImageResource(on ? R.drawable.ic_stat_notify_proximity_silent : R.drawable.ic_stat_notify_proximity_find);
			actionButton.setVisibility(state == BluetoothGatt.STATE_CONNECTED ? View.VISIBLE : View.GONE);

			final int batteryValue = mService.getBatteryValue(device);
			if (batteryValue >= 0) {
				batteryView.getCompoundDrawables()[0 /*left*/].setLevel(batteryValue);
				batteryView.setVisibility(View.VISIBLE);
				batteryView.setText(batteryView.getResources().getString(R.string.battery, batteryValue));
				batteryView.setAlpha(state == BluetoothGatt.STATE_CONNECTED ? 1.0f : 0.5f);
			} else {
				batteryView.setVisibility(View.GONE);
			}
		}
	}
}
