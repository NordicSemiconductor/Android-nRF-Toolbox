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

import java.text.DecimalFormat;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

/**
 * HTSActivity is the main Health Thermometer activity. It implements {@link HTSManagerCallbacks} to receive callbacks from {@link HTSManager} class. The activity supports portrait and landscape
 * orientations.
 */
public class HTSActivity extends BleProfileServiceReadyActivity<HTSService.RSCBinder> {
	@SuppressWarnings("unused")
	private final String TAG = "HTSActivity";

	private TextView mHTSValue;

	@Override
	protected void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_hts);
		setGUI();
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

	private void setGUI() {
		mHTSValue = (TextView) findViewById(R.id.text_hts_value);
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
	protected int getAboutTextId() {
		return R.string.hts_about_text;
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

	private void setHTSValueOnView(final double value) {
		DecimalFormat formattedTemp = new DecimalFormat("#0.00");
		mHTSValue.setText(formattedTemp.format(value));
	}

	@Override
	protected void setDefaultUI() {
		mHTSValue.setText(R.string.not_available_value);
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
