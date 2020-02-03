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
package no.nordicsemi.android.nrftoolbox.proximity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.multiconnect.BleMulticonnectProfileService;
import no.nordicsemi.android.nrftoolbox.profile.multiconnect.BleMulticonnectProfileServiceReadyActivity;
import no.nordicsemi.android.nrftoolbox.widget.DividerItemDecoration;

public class ProximityActivity extends BleMulticonnectProfileServiceReadyActivity<ProximityService.ProximityBinder> {
	private RecyclerView devicesView;
	private DeviceAdapter adapter;

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_proximity);
		setGUI();
	}

	@Override
	protected void onInitialize(final Bundle savedInstanceState) {
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, makeIntentFilter());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
	}

	private void setGUI() {
		final RecyclerView recyclerView = devicesView = findViewById(android.R.id.list);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.proximity_feature_title;
	}

	@Override
	protected void onServiceBound(final ProximityService.ProximityBinder binder) {
		devicesView.setAdapter(adapter = new DeviceAdapter(binder));
	}

	@Override
	protected void onServiceUnbound() {
		devicesView.setAdapter(adapter = null);
	}

	@Override
	protected Class<? extends BleMulticonnectProfileService> getServiceClass() {
		return ProximityService.class;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.proximity_about_text;
	}

	@Override
	protected UUID getFilterUUID() {
		return ProximityManager.LINK_LOSS_SERVICE_UUID;
	}

	@Override
	public void onDeviceConnecting(@NonNull final BluetoothDevice device) {
		if (adapter != null)
			adapter.onDeviceAdded(device);
	}

	@Override
	public void onDeviceConnected(@NonNull final BluetoothDevice device) {
		if (adapter != null)
			adapter.onDeviceStateChanged(device);
	}

	@Override
	public void onDeviceReady(@NonNull final BluetoothDevice device) {
		if (adapter != null)
			adapter.onDeviceStateChanged(device);
	}

	@Override
	public void onDeviceDisconnecting(@NonNull final BluetoothDevice device) {
		if (adapter != null)
			adapter.onDeviceStateChanged(device);
	}

	@Override
	public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
		if (adapter != null)
			adapter.onDeviceRemoved(device);
	}

	@Override
	public void onDeviceNotSupported(@NonNull final BluetoothDevice device) {
		super.onDeviceNotSupported(device);
		if (adapter != null)
			adapter.onDeviceRemoved(device);
	}

	@Override
	public void onLinkLossOccurred(@NonNull final BluetoothDevice device) {
		if (adapter != null)
			adapter.onDeviceStateChanged(device);

		// The link loss may also be called when Bluetooth adapter was disabled
		if (BluetoothAdapter.getDefaultAdapter().isEnabled())
			showLinkLossDialog(device.getName());
	}

	@SuppressWarnings("unused")
	private void onBatteryLevelChanged(final BluetoothDevice device, final int batteryLevel) {
		if (adapter != null)
			adapter.onBatteryValueReceived(device); // Value will be obtained from the service
	}

	@SuppressWarnings("unused")
	private void onRemoteAlarmSwitched(final BluetoothDevice device, final boolean on) {
		if (adapter != null)
			adapter.onDeviceStateChanged(device); // Value will be obtained from the service
	}

	private void showLinkLossDialog(final String name) {
		try {
			final LinkLossDialogFragment dialog = LinkLossDialogFragment.getInstance(name);
			dialog.show(getSupportFragmentManager(), "scan_fragment");
		} catch (final Exception e) {
			// the activity must have been destroyed
		}
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			final BluetoothDevice device = intent.getParcelableExtra(ProximityService.EXTRA_DEVICE);

			if (ProximityService.BROADCAST_BATTERY_LEVEL.equals(action)) {
				final int batteryLevel = intent.getIntExtra(ProximityService.EXTRA_BATTERY_LEVEL, 0);
				// Update GUI
				onBatteryLevelChanged(device, batteryLevel);
			} else if (ProximityService.BROADCAST_ALARM_SWITCHED.equals(action)) {
				final boolean on = intent.getBooleanExtra(ProximityService.EXTRA_ALARM_STATE, false);
				// Update GUI
				onRemoteAlarmSwitched(device, on);
			}
		}
	};

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ProximityService.BROADCAST_BATTERY_LEVEL);
		intentFilter.addAction(ProximityService.BROADCAST_ALARM_SWITCHED);
		return intentFilter;
	}
}
