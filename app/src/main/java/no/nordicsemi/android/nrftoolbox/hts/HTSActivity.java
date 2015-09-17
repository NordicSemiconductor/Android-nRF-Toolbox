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
package no.nordicsemi.android.nrftoolbox.hts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.hts.settings.SettingsActivity;
import no.nordicsemi.android.nrftoolbox.hts.settings.SettingsFragment;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;

/**
 * HTSActivity is the main Health Thermometer activity. It implements {@link HTSManagerCallbacks} to receive callbacks from {@link HTSManager} class. The activity supports portrait and landscape
 * orientations.
 */
public class HTSActivity extends BleProfileServiceReadyActivity<HTSService.RSCBinder> {
	@SuppressWarnings("unused")
	private final String TAG = "HTSActivity";

	private static final String VALUE = "value";
	private static final DecimalFormat mFormattedTemp = new DecimalFormat("#0.00");
	private TextView mHTSValue;
	private TextView mHTSUnit;
	private Double mValueC;

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_hts);
		setGUI();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mValueC != null)
			outState.putDouble(VALUE, mValueC);
	}

	@Override
	protected void onInitialize(final Bundle savedInstanceState) {
		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, makeIntentFilter());

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(VALUE))
				mValueC = savedInstanceState.getDouble(VALUE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
	}

	private void setGUI() {
		mHTSValue = (TextView) findViewById(R.id.text_hts_value);
		mHTSUnit = (TextView) findViewById(R.id.text_hts_unit);

		if (mValueC != null)
			mHTSValue.setText(String.valueOf(mValueC));
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUnits();
	}

	@Override
	protected void setDefaultUI() {
		mValueC = null;
		mHTSValue.setText(R.string.not_available_value);

		setUnits();
	}

	private void setUnits() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final int unit = Integer.parseInt(preferences.getString(SettingsFragment.SETTINGS_UNIT, String.valueOf(SettingsFragment.SETTINGS_UNIT_DEFAULT)));

		switch (unit) {
			case SettingsFragment.SETTINGS_UNIT_C:
				mHTSUnit.setText(R.string.hts_unit_celsius);
				break;
			case SettingsFragment.SETTINGS_UNIT_F:
				mHTSUnit.setText(R.string.hts_unit_fahrenheit);
				break;
			case SettingsFragment.SETTINGS_UNIT_K:
				mHTSUnit.setText(R.string.hts_unit_kelvin);
				break;
		}
		if (mValueC != null)
			setHTSValueOnView(mValueC);
	}

	@Override
	protected void onServiceBinded(final HTSService.RSCBinder binder) {
		// not used
	}

	@Override
	protected void onServiceUnbinded() {
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
		return HTSManager.HT_SERVICE_UUID;
	}

	@Override
	protected Class<? extends BleProfileService> getServiceClass() {
		return HTSService.class;
	}

	@Override
	public void onServicesDiscovered(boolean optionalServicesFound) {
		// this may notify user or show some views
	}

	private void setHTSValueOnView(double value) {
		mValueC = value;
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final int unit = Integer.parseInt(preferences.getString(SettingsFragment.SETTINGS_UNIT, String.valueOf(SettingsFragment.SETTINGS_UNIT_DEFAULT)));

		switch (unit) {
			case SettingsFragment.SETTINGS_UNIT_F:
				value = value * 1.8 + 32;
				break;
			case SettingsFragment.SETTINGS_UNIT_K:
				value += 273.15;
				break;
			case SettingsFragment.SETTINGS_UNIT_C:
				break;
		}
		mHTSValue.setText(mFormattedTemp.format(value));
	}

	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();

			if (HTSService.BROADCAST_HTS_MEASUREMENT.equals(action)) {
				final double value = intent.getDoubleExtra(HTSService.EXTRA_TEMPERATURE, 0.0f);
				// Update GUI
				setHTSValueOnView(value);
			}
		}
	};

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(HTSService.BROADCAST_HTS_MEASUREMENT);
		return intentFilter;
	}
}
