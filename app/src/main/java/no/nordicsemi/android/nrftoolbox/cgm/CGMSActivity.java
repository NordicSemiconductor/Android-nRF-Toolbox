/*
 * Copyright (c) 2016, Nordic Semiconductor
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

package no.nordicsemi.android.nrftoolbox.cgm;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;
import no.nordicsemi.android.nrftoolbox.proximity.ProximityService;

public class CGMSActivity extends BleProfileServiceReadyActivity<CGMService.CGMSBinder> implements PopupMenu.OnMenuItemClickListener {
	private View controlPanelStd;
	private View controlPanelAbort;
	private ListView recordsListView;
	private TextView batteryLevelView;
	private CGMRecordsAdapter CGMRecordsAdapter;

	private CGMService.CGMSBinder binder;

	@Override
	protected void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_cgms);
		setGUI();
	}

	@Override
	protected void onInitialize(Bundle savedInstanceState) {
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, makeIntentFilter());
	}

	private void setGUI() {
		recordsListView = findViewById(R.id.list);
		controlPanelStd = findViewById(R.id.cgms_control_std);
		controlPanelAbort = findViewById(R.id.cgms_control_abort);
		batteryLevelView = findViewById(R.id.battery);

		findViewById(R.id.action_last).setOnClickListener(v -> {
			clearRecords();
			if (binder != null) {
				binder.clear();
				binder.getLastRecord();
			}
		});
		findViewById(R.id.action_all).setOnClickListener(v -> {
			clearRecords();
			if (binder != null) {
				clearRecords();
				binder.getAllRecords();
			}
		});
		findViewById(R.id.action_abort).setOnClickListener(v -> {
			if (binder != null) {
				binder.abort();
			}
		});

		// create popup menu attached to the button More
		findViewById(R.id.action_more).setOnClickListener(v -> {
			PopupMenu menu = new PopupMenu(CGMSActivity.this, v);
			menu.setOnMenuItemClickListener(CGMSActivity.this);
			MenuInflater inflater = menu.getMenuInflater();
			inflater.inflate(R.menu.gls_more, menu.getMenu());
			menu.show();
		});
	}

	private void loadAdapter(SparseArray<CGMRecord> records) {
		CGMRecordsAdapter.clear();
		for (int i = 0; i < records.size(); i++) {
			CGMRecordsAdapter.addItem(records.valueAt(i));
		}
		CGMRecordsAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
	}

	@Override
	protected void onServiceBound(final CGMService.CGMSBinder binder) {
		this.binder = binder;
		final SparseArray<CGMRecord> cgmsRecords = binder.getRecords();
		if (cgmsRecords != null && cgmsRecords.size() > 0) {
			if (CGMRecordsAdapter == null) {
				CGMRecordsAdapter = new CGMRecordsAdapter(CGMSActivity.this);
				recordsListView.setAdapter(CGMRecordsAdapter);
			}
			loadAdapter(cgmsRecords);
		}
	}

	@Override
	protected void onServiceUnbound() {
		binder = null;
	}

	@Override
	protected Class<? extends BleProfileService> getServiceClass() {
		return CGMService.class;
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.cgms_feature_title;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.cgms_about_text;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.cgms_default_name;
	}

	@Override
	protected UUID getFilterUUID() {
		return CGMManager.CGMS_UUID;
	}

	@Override
	public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
		// this may notify user or show some views
	}

	private void setOperationInProgress(final boolean progress) {
		runOnUiThread(() -> {
			// setSupportProgressBarIndeterminateVisibility(progress);
			controlPanelStd.setVisibility(!progress ? View.VISIBLE : View.GONE);
			controlPanelAbort.setVisibility(progress ? View.VISIBLE : View.GONE);
		});
	}

	public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int value) {
		batteryLevelView.setText(getString(R.string.battery, value));
	}

	@Override
	public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
		super.onDeviceDisconnected(device);
		setOperationInProgress(false);
		batteryLevelView.setText(R.string.not_available);
	}

	@Override
	public void onError(@NonNull final BluetoothDevice device, @NonNull final String message, final int errorCode) {
		super.onError(device, message, errorCode);
		setOperationInProgress(false);
	}

	@Override
	protected void setDefaultUI() {
		clearRecords();
		batteryLevelView.setText(R.string.not_available);
	}

	@Override
	public boolean onMenuItemClick(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.action_refresh:
				if(binder != null)
                	binder.refreshRecords();
				break;
			case R.id.action_first:
				if (binder != null)
					binder.getFirstRecord();
				break;
			case R.id.action_clear:
				if (binder != null)
					binder.clear();
				break;
			case R.id.action_delete_all:
				if (binder != null)
					binder.deleteAllRecords();
				break;
		}
		return true;
	}

	private void clearRecords() {
		if (CGMRecordsAdapter != null) {
			CGMRecordsAdapter.clear();
			CGMRecordsAdapter.notifyDataSetChanged();
		}
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			final BluetoothDevice device = intent.getParcelableExtra(ProximityService.EXTRA_DEVICE);

			switch (action) {
				case CGMService.BROADCAST_NEW_CGMS_VALUE: {
					CGMRecord CGMRecord = intent.getExtras().getParcelable(CGMService.EXTRA_CGMS_RECORD);
					if (CGMRecordsAdapter == null) {
						CGMRecordsAdapter = new CGMRecordsAdapter(CGMSActivity.this);
						recordsListView.setAdapter(CGMRecordsAdapter);
					}
					CGMRecordsAdapter.addItem(CGMRecord);
					CGMRecordsAdapter.notifyDataSetChanged();
					break;
				}
				case CGMService.BROADCAST_DATA_SET_CLEAR:
					// Update GUI
					clearRecords();
					break;
				case CGMService.OPERATION_STARTED:
					// Update GUI
					setOperationInProgress(true);
					break;
				case CGMService.BROADCAST_BATTERY_LEVEL:
					final int batteryLevel = intent.getIntExtra(CGMService.EXTRA_BATTERY_LEVEL, 0);
					// Update GUI
					onBatteryLevelChanged(device, batteryLevel);
					break;
				case CGMService.OPERATION_FAILED:
					// Update GUI
					showToast(R.string.gls_operation_failed);
					// breakthrough intended
				default:
					// Update GUI
					setOperationInProgress(false);
			}
		}
	};

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CGMService.BROADCAST_NEW_CGMS_VALUE);
		intentFilter.addAction(CGMService.BROADCAST_DATA_SET_CLEAR);
		intentFilter.addAction(CGMService.OPERATION_STARTED);
		intentFilter.addAction(CGMService.OPERATION_COMPLETED);
		intentFilter.addAction(CGMService.OPERATION_SUPPORTED);
		intentFilter.addAction(CGMService.OPERATION_NOT_SUPPORTED);
		intentFilter.addAction(CGMService.OPERATION_ABORTED);
		intentFilter.addAction(CGMService.OPERATION_FAILED);
		intentFilter.addAction(CGMService.BROADCAST_BATTERY_LEVEL);
		return intentFilter;
	}
}
