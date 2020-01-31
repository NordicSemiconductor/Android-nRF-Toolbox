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
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import no.nordicsemi.android.nrftoolbox.R;

class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
	private final ProximityService.ProximityBinder service;
	private final List<BluetoothDevice> devices;

	DeviceAdapter(@NonNull final ProximityService.ProximityBinder binder) {
		service = binder;
		devices = service.getManagedDevices();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
		final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_feature_proximity_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
		holder.bind(devices.get(position));
	}

	@Override
	public int getItemCount() {
		return devices.size();
	}

	void onDeviceAdded(final BluetoothDevice device) {
		final int position = devices.indexOf(device);
		if (position == -1) {
			notifyItemInserted(devices.size() - 1);
		} else {
			// This may happen when Bluetooth adapter was switched off and on again
			// while there were devices on the list.
			notifyItemChanged(position);
		}
	}

	void onDeviceRemoved(final BluetoothDevice device) {
		notifyDataSetChanged(); // we don't have position of the removed device here
	}

	void onDeviceStateChanged(final BluetoothDevice device) {
		final int position = devices.indexOf(device);
		if (position >= 0)
			notifyItemChanged(position);
	}

	void onBatteryValueReceived(final BluetoothDevice device) {
		final int position = devices.indexOf(device);
		if (position >= 0)
			notifyItemChanged(position);
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private TextView nameView;
		private TextView addressView;
		private TextView batteryView;
		private ImageButton actionButton;
		private ProgressBar progress;

		ViewHolder(final View itemView) {
			super(itemView);

			nameView = itemView.findViewById(R.id.name);
			addressView = itemView.findViewById(R.id.address);
			batteryView = itemView.findViewById(R.id.battery);
			actionButton = itemView.findViewById(R.id.action_find_silent);
			progress = itemView.findViewById(R.id.progress);

			// Configure FIND / SILENT button
			actionButton.setOnClickListener(v -> {
				final int position = getAdapterPosition();
				final BluetoothDevice device = devices.get(position);
				service.toggleImmediateAlert(device);
			});

			// Configure Disconnect button
			itemView.findViewById(R.id.action_disconnect).setOnClickListener(v -> {
				final int position = getAdapterPosition();
				final BluetoothDevice device = devices.get(position);
				service.disconnect(device);
				// The device might have not been connected, so there will be no callback
				onDeviceRemoved(device);
			});
		}

		private void bind(@NonNull final BluetoothDevice device) {
			final boolean ready = service.isReady(device);

			String name = device.getName();
			if (TextUtils.isEmpty(name))
				name = nameView.getResources().getString(R.string.proximity_default_device_name);
			nameView.setText(name);
			addressView.setText(device.getAddress());

			final boolean on = service.isImmediateAlertOn(device);
			actionButton.setImageResource(on ? R.drawable.ic_stat_notify_proximity_silent : R.drawable.ic_stat_notify_proximity_find);
			actionButton.setVisibility(ready ? View.VISIBLE : View.GONE);
			progress.setVisibility(ready ? View.GONE : View.VISIBLE);

			final Integer batteryValue = service.getBatteryLevel(device);
			if (batteryValue != null) {
				batteryView.getCompoundDrawables()[0 /*left*/].setLevel(batteryValue);
				batteryView.setVisibility(View.VISIBLE);
				batteryView.setText(batteryView.getResources().getString(R.string.battery, batteryValue));
				batteryView.setAlpha(ready ? 1.0f : 0.5f);
			} else {
				batteryView.setVisibility(View.GONE);
			}
		}
	}
}
