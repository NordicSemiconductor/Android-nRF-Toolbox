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
package no.nordicsemi.android.nrftoolbox.ht;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.Menu;
import android.widget.TextView;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.ht.settings.SettingsActivity;
import no.nordicsemi.android.nrftoolbox.ht.settings.SettingsFragment;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;

/**
 * HTSActivity is the main Health Thermometer activity. It implements {@link HTManagerCallbacks}
 * to receive callbacks from {@link HTManager} class. The activity supports portrait and landscape
 * orientations.
 */
public class HTActivity extends BleProfileServiceReadyActivity<HTService.HTSBinder> {
	@SuppressWarnings("unused")
	private final String TAG = "HTSActivity";

	private TextView tempValueView;
	private TextView unitView;
	private TextView batteryLevelView;

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_hts);
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
		tempValueView = findViewById(R.id.text_hts_value);
		unitView = findViewById(R.id.text_hts_unit);
		batteryLevelView = findViewById(R.id.battery);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUnits();
	}

	@Override
	protected void setDefaultUI() {
		tempValueView.setText(R.string.not_available_value);
		batteryLevelView.setText(R.string.not_available);

		setUnits();
	}

	private void setUnits() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final int unit = Integer.parseInt(preferences.getString(SettingsFragment.SETTINGS_UNIT, String.valueOf(SettingsFragment.SETTINGS_UNIT_DEFAULT)));

		switch (unit) {
			case SettingsFragment.SETTINGS_UNIT_C:
				this.unitView.setText(R.string.hts_unit_celsius);
				break;
			case SettingsFragment.SETTINGS_UNIT_F:
				this.unitView.setText(R.string.hts_unit_fahrenheit);
				break;
			case SettingsFragment.SETTINGS_UNIT_K:
				this.unitView.setText(R.string.hts_unit_kelvin);
				break;
		}
	}

	@Override
	protected void onServiceBound(final HTService.HTSBinder binder) {
		onTemperatureMeasurementReceived(binder.getTemperature());
	}

	@Override
	protected void onServiceUnbound() {
		// not used
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.hts_feature_title;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.hts_about_text;
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
		return R.string.hts_default_name;
	}

	@Override
	protected UUID getFilterUUID() {
		return HTManager.HT_SERVICE_UUID;
	}

	@Override
	protected Class<? extends BleProfileService> getServiceClass() {
		return HTService.class;
	}

	@Override
	public void onServicesDiscovered(@NonNull final BluetoothDevice device, boolean optionalServicesFound) {
		// this may notify user or show some views
	}

	@Override
	public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
		super.onDeviceDisconnected(device);
		batteryLevelView.setText(R.string.not_available);
	}

	private void onTemperatureMeasurementReceived(Float value) {
		if (value != null) {
			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			final int unit = Integer.parseInt(preferences.getString(SettingsFragment.SETTINGS_UNIT,
					String.valueOf(SettingsFragment.SETTINGS_UNIT_DEFAULT)));

			switch (unit) {
				case SettingsFragment.SETTINGS_UNIT_F:
					value = value * 1.8f + 32f;
					break;
				case SettingsFragment.SETTINGS_UNIT_K:
					value += 273.15f;
					break;
				case SettingsFragment.SETTINGS_UNIT_C:
					break;
			}
			tempValueView.setText(getString(R.string.hts_value, value));
		} else {
			tempValueView.setText(R.string.not_available_value);
		}
	}

	public void onBatteryLevelChanged(final int value) {
		batteryLevelView.setText(getString(R.string.battery, value));
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();

			if (HTService.BROADCAST_HTS_MEASUREMENT.equals(action)) {
				final float value = intent.getFloatExtra(HTService.EXTRA_TEMPERATURE, 0.0f);
				// Update GUI
				onTemperatureMeasurementReceived(value);
			} else if (HTService.BROADCAST_BATTERY_LEVEL.equals(action)) {
				final int batteryLevel = intent.getIntExtra(HTService.EXTRA_BATTERY_LEVEL, 0);
				// Update GUI
				onBatteryLevelChanged(batteryLevel);
			}
		}
	};

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(HTService.BROADCAST_HTS_MEASUREMENT);
		intentFilter.addAction(HTService.BROADCAST_BATTERY_LEVEL);
		return intentFilter;
	}
}
