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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class ProximityActivity extends BleProfileServiceReadyActivity<ProximityService.ProximityBinder> {
	private static final String TAG = "ProximityActivity";

	public static final String PREFS_GATT_SERVER_ENABLED = "prefs_gatt_server_enabled";

	private Button mFindMeButton;
	private ImageView mLockImage;
	private CheckBox mGattServerSwitch;

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_proximity);
		setGUI();
	}

	private void setGUI() {
		mFindMeButton = (Button) findViewById(R.id.action_findme);
		mLockImage = (ImageView) findViewById(R.id.imageLock);
		mGattServerSwitch = (CheckBox) findViewById(R.id.option);

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ProximityActivity.this);
		mGattServerSwitch.setChecked(preferences.getBoolean(PREFS_GATT_SERVER_ENABLED, true));
		mGattServerSwitch.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
				preferences.edit().putBoolean(PREFS_GATT_SERVER_ENABLED, isChecked).apply();
			}
		});
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.proximity_feature_title;
	}

	@Override
	protected void onServiceBinded(final ProximityService.ProximityBinder binder) {
		mGattServerSwitch.setEnabled(false);

		if (binder.isConnected()) {
			showOpenLock();

			if (binder.isImmediateAlertOn()) {
				showSilentMeOnButton();
			}
		}
	}

	@Override
	protected void onServiceUnbinded() {
		// you may release the binder instance here
	}

	@Override
	protected Class<? extends BleProfileService> getServiceClass() {
		return ProximityService.class;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.proximity_about_text;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.proximity_default_name;
	}

	@Override
	protected UUID getFilterUUID() {
		return ProximityManager.LINKLOSS_SERVICE_UUID;
	}

	/**
	 * Callback of FindMe button on ProximityActivity
	 */
	public void onFindMeClicked(final View view) {
		if (isBLEEnabled()) {
			if (!isDeviceConnected()) {
				// do nothing
			} else if (getService().toggleImmediateAlert()) {
				showSilentMeOnButton();
			} else {
				showFindMeOnButton();
			}
		} else {
			showBLEDialog();
		}
	}

	@Override
	protected void setDefaultUI() {
		mFindMeButton.setText(R.string.proximity_action_findme);
		mLockImage.setImageResource(R.drawable.proximity_lock_closed);
	}

	@Override
	public void onServicesDiscovered(boolean optionalServicesFound) {
		// this may notify user or update views
	}

	@Override
	public void onDeviceReady() {
		showOpenLock();
	}

	@Override
	public void onDeviceDisconnected() {
		super.onDeviceDisconnected();
		showClosedLock();
		mGattServerSwitch.setEnabled(true);
	}

	@Override
	public void onBondingRequired() {
		showClosedLock();
	}

	@Override
	public void onBonded() {
		showOpenLock();
	}

	@Override
	public void onLinklossOccur() {
		super.onLinklossOccur();
		showClosedLock();
		resetForLinkloss();

		DebugLogger.w(TAG, "Linkloss occur");

		String deviceName = getDeviceName();
		if (deviceName == null) {
			deviceName = getString(R.string.proximity_default_name);
		}

		showLinklossDialog(deviceName);
	}

	private void resetForLinkloss() {
		setDefaultUI();
	}

	private void showFindMeOnButton() {
		mFindMeButton.setText(R.string.proximity_action_findme);
	}

	private void showSilentMeOnButton() {
		mFindMeButton.setText(R.string.proximity_action_silentme);
	}

	private void showOpenLock() {
		mFindMeButton.setEnabled(true);
		mLockImage.setImageResource(R.drawable.proximity_lock_open);
	}

	private void showClosedLock() {
		mFindMeButton.setEnabled(false);
		mLockImage.setImageResource(R.drawable.proximity_lock_closed);
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
