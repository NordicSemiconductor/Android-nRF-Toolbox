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
package no.nordicsemi.android.nrftoolbox.template;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.Menu;
import android.widget.TextView;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;
import no.nordicsemi.android.nrftoolbox.template.settings.SettingsActivity;

/**
 * Modify the Template Activity to match your needs.
 */
public class TemplateActivity extends BleProfileServiceReadyActivity<TemplateService.TemplateBinder> {
	@SuppressWarnings("unused")
	private final String TAG = "TemplateActivity";

	// TODO change view references to match your need
	private TextView valueView;
	private TextView batteryLevelView;

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		// TODO modify the layout file(s). By default the activity shows only one field - the Heart Rate value as a sample
		setContentView(R.layout.activity_feature_template);
		setGUI();
	}

	private void setGUI() {
		// TODO assign your views to fields
		valueView = findViewById(R.id.value);
		batteryLevelView = findViewById(R.id.battery);

		findViewById(R.id.action_set_name).setOnClickListener(v -> {
			if (isDeviceConnected()) {
				getService().performAction("Template");
			}
		});
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

	@Override
	protected void setDefaultUI() {
		// TODO clear your UI
		valueView.setText(R.string.not_available_value);
		batteryLevelView.setText(R.string.not_available);
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.template_feature_title;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.template_about_text;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.settings_and_about, menu);
		return true;
	}

	@Override
	protected boolean onOptionsItemSelected(final int itemId) {
		switch (itemId) {
			case R.id.action_settings:
				final Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				break;
		}
		return true;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.template_default_name;
	}

	@Override
	protected UUID getFilterUUID() {
		// TODO this method may return the UUID of the service that is required to be in the advertisement packet of a device in order to be listed on the Scanner dialog.
		// If null is returned no filtering is done.
		return TemplateManager.SERVICE_UUID;
	}

	@Override
	protected Class<? extends BleProfileService> getServiceClass() {
		return TemplateService.class;
	}

	@Override
	protected void onServiceBound(final TemplateService.TemplateBinder binder) {
		// not used
	}

	@Override
	protected void onServiceUnbound() {
		// not used
	}

	@Override
	public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
		// this may notify user or show some views
	}

	@Override
	public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
		super.onDeviceDisconnected(device);
		batteryLevelView.setText(R.string.not_available);
	}

	// Handling updates from the device
	@SuppressWarnings("unused")
	private void setValueOnView(@NonNull final BluetoothDevice device, final int value) {
		// TODO assign the value to a view
		valueView.setText(String.valueOf(value));
	}

	@SuppressWarnings("unused")
	public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int value) {
		batteryLevelView.setText(getString(R.string.battery, value));
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			final BluetoothDevice device = intent.getParcelableExtra(TemplateService.EXTRA_DEVICE);

			if (TemplateService.BROADCAST_TEMPLATE_MEASUREMENT.equals(action)) {
				final int value = intent.getIntExtra(TemplateService.EXTRA_DATA, 0);
				// Update GUI
				setValueOnView(device, value);
			} else if (TemplateService.BROADCAST_BATTERY_LEVEL.equals(action)) {
				final int batteryLevel = intent.getIntExtra(TemplateService.EXTRA_BATTERY_LEVEL, 0);
				// Update GUI
				onBatteryLevelChanged(device, batteryLevel);
			}
		}
	};

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(TemplateService.BROADCAST_TEMPLATE_MEASUREMENT);
		intentFilter.addAction(TemplateService.BROADCAST_BATTERY_LEVEL);
		return intentFilter;
	}
}
