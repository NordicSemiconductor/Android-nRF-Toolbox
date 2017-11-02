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
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.multiconnect.BleMulticonnectProfileService;
import no.nordicsemi.android.nrftoolbox.profile.multiconnect.BleMulticonnectProfileServiceReadyActivity;
import no.nordicsemi.android.nrftoolbox.widget.DividerItemDecoration;

public class ProximityActivity extends BleMulticonnectProfileServiceReadyActivity<ProximityService.ProximityBinder> {
	private static final String TAG = "ProximityActivity";

	// This is not used any more. Server is created always after the service is started or
	// after Bluetooth adapter is enabled.
	// public static final String PREFS_GATT_SERVER_ENABLED = "prefs_gatt_server_enabled";

	private RecyclerView mDevicesView;
	private DeviceAdapter mAdapter;

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_proximity);
		setGUI();
	}

	private void setGUI() {
		final RecyclerView recyclerView = mDevicesView = findViewById(android.R.id.list);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.proximity_feature_title;
	}

	@Override
	protected void onServiceBinded(final ProximityService.ProximityBinder binder) {
		mDevicesView.setAdapter(mAdapter = new DeviceAdapter(binder));
	}

	@Override
	protected void onServiceUnbinded() {
		mDevicesView.setAdapter(mAdapter = null);
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
		return ProximityManager.LINKLOSS_SERVICE_UUID;
	}

	@Override
	public void onDeviceConnecting(final BluetoothDevice device) {
		if (mAdapter != null)
			mAdapter.onDeviceAdded(device);
	}

	@Override
	public void onDeviceConnected(final BluetoothDevice device) {
		if (mAdapter != null)
			mAdapter.onDeviceStateChanged(device);
	}

	@Override
	public void onDeviceReady(final BluetoothDevice device) {
		if (mAdapter != null)
			mAdapter.onDeviceStateChanged(device);
	}

	@Override
	public void onDeviceDisconnecting(final BluetoothDevice device) {
		if (mAdapter != null)
			mAdapter.onDeviceStateChanged(device);
	}

	@Override
	public void onDeviceDisconnected(final BluetoothDevice device) {
		if (mAdapter != null)
			mAdapter.onDeviceRemoved(device);
	}

	@Override
	public void onDeviceNotSupported(final BluetoothDevice device) {
		super.onDeviceNotSupported(device);
		if (mAdapter != null)
			mAdapter.onDeviceRemoved(device);
	}

	@Override
	public void onLinklossOccur(final BluetoothDevice device) {
		if (mAdapter != null)
			mAdapter.onDeviceStateChanged(device);

		// The link loss may also be called when Bluetooth adapter was disabled
		if (BluetoothAdapter.getDefaultAdapter().isEnabled())
			showLinklossDialog(device.getName());
	}

	@Override
	public void onBatteryValueReceived(final BluetoothDevice device, final int value) {
		if (mAdapter != null)
			mAdapter.onBatteryValueReceived(device); // Value will be obtained from the service
	}

	private void showLinklossDialog(final String name) {
		try {
			final LinklossFragment dialog = LinklossFragment.getInstance(name);
			dialog.show(getSupportFragmentManager(), "scan_fragment");
		} catch (final Exception e) {
			// the activity must have been destroyed
		}
	}
}
