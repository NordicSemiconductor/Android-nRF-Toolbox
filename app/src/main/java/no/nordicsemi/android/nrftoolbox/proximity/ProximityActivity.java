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

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

public class ProximityActivity extends BleProfileServiceReadyActivity<ProximityService.ProximityBinder> {
	private static final String TAG = "ProximityActivity";

	public static final String PREFS_GATT_SERVER_ENABLED = "prefs_gatt_server_enabled";
	private static final String IMMEDIATE_ALERT_STATUS = "immediate_alert_status";
	private boolean isImmediateAlertOn = false;

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
				preferences.edit().putBoolean(PREFS_GATT_SERVER_ENABLED, isChecked).commit();
			}
		});
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(IMMEDIATE_ALERT_STATUS, isImmediateAlertOn);
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		isImmediateAlertOn = savedInstanceState.getBoolean(IMMEDIATE_ALERT_STATUS);
		if (isDeviceConnected()) {
			showOpenLock();

			if (isImmediateAlertOn) {
				showSilentMeOnButton();
			}
		}
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.proximity_feature_title;
	}

	@Override
	protected void onServiceBinded(final ProximityService.ProximityBinder binder) {
		// you may get the binder instance here 
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
			} else if (!isImmediateAlertOn) {
				showSilentMeOnButton();
				((ProximityService.ProximityBinder) getService()).startImmediateAlert();
				isImmediateAlertOn = true;
			} else {
				showFindMeOnButton();
				((ProximityService.ProximityBinder) getService()).stopImmediateAlert();
				isImmediateAlertOn = false;
			}
		} else {
			showBLEDialog();
		}
	}

	@Override
	protected void setDefaultUI() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mFindMeButton.setText(R.string.proximity_action_findme);
				mLockImage.setImageResource(R.drawable.proximity_lock_closed);
			}
		});
	}

	@Override
	public void onServicesDiscovered(boolean optionalServicesFound) {
		// this may notify user or update views
	}

	@Override
	public void onDeviceConnected() {
		super.onDeviceConnected();
		showOpenLock();
		mGattServerSwitch.setEnabled(false);
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
		isImmediateAlertOn = false;
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
			FragmentManager fm = getFragmentManager();
			LinklossFragment dialog = LinklossFragment.getInstance(name);
			dialog.show(fm, "scan_fragment");
		} catch (final Exception e) {
			// the activity must have been destroyed
		}
	}
}
