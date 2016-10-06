///*
// * Copyright (c) 2015, Nordic Semiconductor
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
// *
// * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
// *
// * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
// * documentation and/or other materials provided with the distribution.
// *
// * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
// * software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
// * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package com.ibm.android.cntv.tibet.client.template;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.support.v4.content.LocalBroadcastManager;
//import android.view.Menu;
//import android.widget.TextView;
//
//import java.util.UUID;
//
//import com.ibm.android.cntv.tibet.client.R;
//import com.ibm.android.cntv.tibet.client.profile.BleProfileService;
//import com.ibm.android.cntv.tibet.client.profile.BleProfileServiceReadyActivity;
//import com.ibm.android.cntv.tibet.client.template.settings.SettingsActivity;
//
///**
// * Modify the Template Activity to match your needs.
// */
//public class TemplateActivity extends BleProfileServiceReadyActivity<TemplateService.TemplateBinder> {
//	@SuppressWarnings("unused")
//	private final String TAG = "TemplateActivity";
//
//	// TODO change view references to match your need
//	private TextView mValueView;
//	private TextView mValueUnitView;
//
//	@Override
//	protected void onCreateView(final Bundle savedInstanceState) {
//		// TODO modify the layout file(s). By default the activity shows only one field - the Heart Rate value as a sample
//		setContentView(R.layout.activity_feature_template);
//		setGUI();
//	}
//
//	private void setGUI() {
//		// TODO assign your views to fields
//		mValueView = (TextView) findViewById(R.id.value);
//		mValueUnitView = (TextView) findViewById(R.id.value_unit);
//	}
//
//	@Override
//	protected void onInitialize(final Bundle savedInstanceState) {
//		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, makeIntentFilter());
//	}
//
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
//	}
//
//	@Override
//	protected void setDefaultUI() {
//		// TODO clear your UI
//		mValueView.setText(R.string.not_available_value);
//	}
//
//	@Override
//	protected void onServiceBinded(final TemplateService.TemplateBinder binder) {
//		// not used
//	}
//
//	@Override
//	protected void onServiceUnbinded() {
//		// not used
//	}
//
//	@Override
//	protected int getLoggerProfileTitle() {
//		return R.string.template_feature_title;
//	}
//
//	@Override
//	protected int getAboutTextId() {
//		return R.string.template_about_text;
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(final Menu menu) {
//		getMenuInflater().inflate(R.menu.settings_and_about, menu);
//		return true;
//	}
//
//	@Override
//	protected boolean onOptionsItemSelected(final int itemId) {
//		switch (itemId) {
//			case R.id.action_settings:
//				final Intent intent = new Intent(this, SettingsActivity.class);
//				startActivity(intent);
//				break;
//		}
//		return true;
//	}
//
//	@Override
//	protected int getDefaultDeviceName() {
//		return R.string.template_default_name;
//	}
//
//	@Override
//	protected UUID getFilterUUID() {
//		// TODO this method may return the UUID of the service that is required to be in the advertisement packet of a device in order to be listed on the Scanner dialog.
//		// If null is returned no filtering is done.
//		return TemplateManager.SERVICE_UUID;
//	}
//
//	@Override
//	protected Class<? extends BleProfileService> getServiceClass() {
//		return TemplateService.class;
//	}
//
//	@Override
//	public void onServicesDiscovered(boolean optionalServicesFound) {
//		// this may notify user or show some views
//	}
//
//	private void setValueOnView(final int value) {
//		// TODO assign the value to a view
//		mValueView.setText(String.valueOf(value));
//	}
//
//	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(final Context context, final Intent intent) {
//			final String action = intent.getAction();
//
//			if (TemplateService.BROADCAST_TEMPLATE_MEASUREMENT.equals(action)) {
//				final int value = intent.getIntExtra(TemplateService.EXTRA_DATA, 0);
//				// Update GUI
//				setValueOnView(value);
//			}
//		}
//	};
//
//	private static IntentFilter makeIntentFilter() {
//		final IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction(TemplateService.BROADCAST_TEMPLATE_MEASUREMENT);
//		return intentFilter;
//	}
//}
