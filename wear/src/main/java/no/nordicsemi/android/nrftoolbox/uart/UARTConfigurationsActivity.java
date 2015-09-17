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

package no.nordicsemi.android.nrftoolbox.uart;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WearableListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.ble.BleProfileService;
import no.nordicsemi.android.nrftoolbox.wearable.common.Constants;
import no.nordicsemi.android.nrftoolbox.uart.domain.UartConfiguration;

public class UARTConfigurationsActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
		DataApi.DataListener, GoogleApiClient.OnConnectionFailedListener, WearableListView.ClickListener, MessageApi.MessageListener {
	private UARTConfigurationsAdapter mAdapter;
	private GoogleApiClient mGoogleApiClient;
	private BleProfileService.LocalBinder mBinder;

	private BroadcastReceiver mServiceBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();

			switch (action) {
				case BleProfileService.BROADCAST_CONNECTION_STATE: {
					final int state = intent.getIntExtra(BleProfileService.EXTRA_CONNECTION_STATE, BleProfileService.STATE_DISCONNECTED);
					if (state == BleProfileService.STATE_DISCONNECTED)
						finish();
					break;
				}
				case BleProfileService.BROADCAST_ERROR: {
					final String message = intent.getStringExtra(BleProfileService.EXTRA_ERROR_MESSAGE);
					// final int errorCode = intent.getIntExtra(BleProfileService.EXTRA_ERROR_CODE, 0);
					Toast.makeText(UARTConfigurationsActivity.this, message, Toast.LENGTH_SHORT).show();
					// TODO error handing
					break;
				}
			}
		}
	};

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName name, final IBinder service) {
			mBinder = (BleProfileService.LocalBinder) service;
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			mBinder = null;
		}
	};

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		// Check if the WEAR device is connected to the UART device itself, or by the phone.
		// Binding will fail if we are using phone as proxy as the service has not been started before.
		final Intent service = new Intent(this, BleProfileService.class);
		bindService(service, mServiceConnection, 0);

		final WearableListView listView = (WearableListView) findViewById(R.id.list);
		listView.setClickListener(this);
		listView.setAdapter(mAdapter = new UARTConfigurationsAdapter(this));

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();


		// Register the broadcast receiver that will listen for events from the device
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BleProfileService.BROADCAST_CONNECTION_STATE);
		filter.addAction(BleProfileService.BROADCAST_ERROR);
		LocalBroadcastManager.getInstance(this).registerReceiver(mServiceBroadcastReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mGoogleApiClient.unregisterConnectionCallbacks(this);
		mGoogleApiClient.unregisterConnectionFailedListener(this);
		mGoogleApiClient = null;

		// If we were binded to the service, disconnect and unbind. The service will terminate itself when disconnected.
		if (mBinder != null) {
			mBinder.disconnect();
		}
		unbindService(mServiceConnection);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceBroadcastReceiver);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Wearable.MessageApi.removeListener(mGoogleApiClient, this);
		Wearable.DataApi.removeListener(mGoogleApiClient, this);
		mGoogleApiClient.disconnect();
	}

	@Override
	public void onConnected(final Bundle bundle) {
		Wearable.DataApi.addListener(mGoogleApiClient, this);
		Wearable.MessageApi.addListener(mGoogleApiClient, this);
		populateConfigurations();
	}

	@Override
	public void onConnectionSuspended(final int cause) {
		Wearable.DataApi.removeListener(mGoogleApiClient, this);
		finish();
	}

	@Override
	public void onConnectionFailed(final ConnectionResult connectionResult) {
		finish();
	}

	@Override
	public void onDataChanged(final DataEventBuffer dataEventBuffer) {
		populateConfigurations();
	}

	@Override
	public void onMessageReceived(final MessageEvent messageEvent) {
		// If the activity is binded to service it means that it has connected directly to the device. We ignore messages from the handheld.
		if (mBinder != null)
			return;

		switch (messageEvent.getPath()) {
			case Constants.UART.DEVICE_LINKLOSS:
			case Constants.UART.DEVICE_DISCONNECTED: {
				finish();
				break;
			}
		}
	}

	@Override
	public void onClick(final WearableListView.ViewHolder viewHolder) {
		if (viewHolder instanceof UARTConfigurationsAdapter.ConfigurationViewHolder) {
			final UARTConfigurationsAdapter.ConfigurationViewHolder holder = (UARTConfigurationsAdapter.ConfigurationViewHolder) viewHolder;
			final UartConfiguration configuration = holder.getConfiguration();

			final Intent intent = new Intent(this, UARTCommandsActivity.class);
			intent.putExtra(UARTCommandsActivity.CONFIGURATION, configuration);
			startActivity(intent);
		}
	}

	@Override
	public void onTopEmptyRegionClick() {
		// do nothing
	}

	/**
	 * This method read the UART configurations from the DataApi and populates the adapter with them.
	 */
	private void populateConfigurations() {
		if (mGoogleApiClient.isConnected()) {
			final PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient, Uri.parse("wear:" + Constants.UART.CONFIGURATIONS), DataApi.FILTER_PREFIX);
			results.setResultCallback(new ResultCallback<DataItemBuffer>() {
				@Override
				public void onResult(final DataItemBuffer dataItems) {
					final List<UartConfiguration> configurations = new ArrayList<>(dataItems.getCount());
					for (int i = 0; i < dataItems.getCount(); ++i) {
						final DataItem item = dataItems.get(i);
						final long id = ContentUris.parseId(item.getUri());
						final DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
						final UartConfiguration configuration = new UartConfiguration(dataMap, id);
						configurations.add(configuration);
					}
					mAdapter.setConfigurations(configurations);
					dataItems.release();
				}
			});
		}
	}
}
