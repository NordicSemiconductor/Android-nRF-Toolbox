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

package no.nordicsemi.android.nrftoolbox.csc;

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

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.csc.settings.SettingsActivity;
import no.nordicsemi.android.nrftoolbox.csc.settings.SettingsFragment;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;

public class CSCActivity extends BleProfileServiceReadyActivity<CSCService.CSCBinder> {
	private TextView mSpeedView;
	private TextView mSpeedUnitView;
	private TextView mCadenceView;
	private TextView mDistanceView;
	private TextView mDistanceUnitView;
	private TextView mTotalDistanceView;
	private TextView mTotalDistanceUnitView;
	private TextView mGearRatioView;

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_csc);
		setGui();
	}

	@Override
	protected void onInitialize(final Bundle savedInstanceState) {
		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, makeIntentFilter());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
	}

	private void setGui() {
		mSpeedView = (TextView) findViewById(R.id.speed);
		mSpeedUnitView = (TextView) findViewById(R.id.speed_unit);
		mCadenceView = (TextView) findViewById(R.id.cadence);
		mDistanceView = (TextView) findViewById(R.id.distance);
		mDistanceUnitView = (TextView) findViewById(R.id.distance_unit);
		mTotalDistanceView = (TextView) findViewById(R.id.distance_total);
		mTotalDistanceUnitView = (TextView) findViewById(R.id.distance_total_unit);
		mGearRatioView = (TextView) findViewById(R.id.ratio);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setDefaultUI();
	}

	@Override
	protected void setDefaultUI() {
		mSpeedView.setText(R.string.not_available_value);
		mCadenceView.setText(R.string.not_available_value);
		mDistanceView.setText(R.string.not_available_value);
		mTotalDistanceView.setText(R.string.not_available_value);
		mGearRatioView.setText(R.string.not_available_value);

		setUnits();
	}

	private void setUnits() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final int unit = Integer.parseInt(preferences.getString(SettingsFragment.SETTINGS_UNIT, String.valueOf(SettingsFragment.SETTINGS_UNIT_DEFAULT)));

		switch (unit) {
			case SettingsFragment.SETTINGS_UNIT_M_S: // [m/s]
				mSpeedUnitView.setText(R.string.csc_speed_unit_m_s);
				mDistanceUnitView.setText(R.string.csc_distance_unit_m);
				mTotalDistanceUnitView.setText(R.string.csc_total_distance_unit_km);
				break;
			case SettingsFragment.SETTINGS_UNIT_KM_H: // [km/h]
				mSpeedUnitView.setText(R.string.csc_speed_unit_km_h);
				mDistanceUnitView.setText(R.string.csc_distance_unit_m);
				mTotalDistanceUnitView.setText(R.string.csc_total_distance_unit_km);
				break;
			case SettingsFragment.SETTINGS_UNIT_MPH: // [mph]
				mSpeedUnitView.setText(R.string.csc_speed_unit_mph);
				mDistanceUnitView.setText(R.string.csc_distance_unit_yd);
				mTotalDistanceUnitView.setText(R.string.csc_total_distance_unit_mile);
				break;
		}
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.csc_feature_title;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.csc_default_name;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.csc_about_text;
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
	protected Class<? extends BleProfileService> getServiceClass() {
		return CSCService.class;
	}

	@Override
	protected UUID getFilterUUID() {
		return CSCManager.CYCLING_SPEED_AND_CADENCE_SERVICE_UUID;
	}

	@Override
	protected void onServiceBinded(final CSCService.CSCBinder binder) {
		// not used
	}

	@Override
	protected void onServiceUnbinded() {
		// not used
	}

	@Override
	public void onServicesDiscovered(final boolean optionalServicesFound) {
		// not used
	}

	private void onMeasurementReceived(float speed, float distance, float totalDistance) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final int unit = Integer.parseInt(preferences.getString(SettingsFragment.SETTINGS_UNIT, String.valueOf(SettingsFragment.SETTINGS_UNIT_DEFAULT)));

		switch (unit) {
			case SettingsFragment.SETTINGS_UNIT_KM_H:
				speed = speed * 3.6f;
				// pass through intended
			case SettingsFragment.SETTINGS_UNIT_M_S:
				if (distance < 1000) { // 1 km in m
					mDistanceView.setText(String.format("%.0f", distance));
					mDistanceUnitView.setText(R.string.csc_distance_unit_m);
				} else {
					mDistanceView.setText(String.format("%.2f", distance / 1000.0f));
					mDistanceUnitView.setText(R.string.csc_distance_unit_km);
				}

				mTotalDistanceView.setText(String.format("%.2f", totalDistance / 1000.0f));
				break;
			case SettingsFragment.SETTINGS_UNIT_MPH:
				speed = speed * 2.2369f;
				if (distance < 1760) { // 1 mile in yrs
					mDistanceView.setText(String.format("%.0f", distance));
					mDistanceUnitView.setText(R.string.csc_distance_unit_yd);
				} else {
					mDistanceView.setText(String.format("%.2f", distance / 1760.0f));
					mDistanceUnitView.setText(R.string.csc_distance_unit_mile);
				}

				mTotalDistanceView.setText(String.format("%.2f", totalDistance / 1609.31f));
				break;
		}

		mSpeedView.setText(String.format("%.1f", speed));
	}

	private void onGearRatioUpdate(final float ratio, final int cadence) {
		mGearRatioView.setText(String.format("%.1f", ratio));
		mCadenceView.setText(String.format("%d", cadence));
	}

	private final  BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();

			if (CSCService.BROADCAST_WHEEL_DATA.equals(action)) {
				final float speed = intent.getFloatExtra(CSCService.EXTRA_SPEED, 0.0f); // [m/s]
				final float distance = intent.getFloatExtra(CSCService.EXTRA_DISTANCE, CSCManagerCallbacks.NOT_AVAILABLE);
				final float totalDistance = intent.getFloatExtra(CSCService.EXTRA_TOTAL_DISTANCE, CSCManagerCallbacks.NOT_AVAILABLE);
				// Update GUI
				onMeasurementReceived(speed, distance, totalDistance);
			} else if (CSCService.BROADCAST_CRANK_DATA.equals(action)) {
				final float ratio = intent.getFloatExtra(CSCService.EXTRA_GEAR_RATIO, 0);
				final int cadence = intent.getIntExtra(CSCService.EXTRA_CADENCE, 0);
				// Update GUI
				onGearRatioUpdate(ratio, cadence);
			}
		}
	};

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CSCService.BROADCAST_WHEEL_DATA);
		intentFilter.addAction(CSCService.BROADCAST_CRANK_DATA);
		return intentFilter;
	}
}
