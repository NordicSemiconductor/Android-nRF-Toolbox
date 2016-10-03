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

package com.ibm.android.cntv.tibet.client.uart;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import com.ibm.android.cntv.tibet.client.R;
import com.ibm.android.cntv.tibet.client.ble.BleProfileService;
import com.ibm.android.cntv.tibet.client.wearable.common.Constants;
import com.ibm.android.cntv.tibet.client.uart.domain.Command;
import com.ibm.android.cntv.tibet.client.uart.domain.UartConfiguration;

public class UARTCommandsActivity extends Activity implements UARTCommandsAdapter.OnCommandSelectedListener, GoogleApiClient.ConnectionCallbacks,
		DataApi.DataListener, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {
	private static final String TAG = "UARTCommandsActivity";

	public static final String CONFIGURATION = "configuration";

	private GoogleApiClient mGoogleApiClient;
	private UARTCommandsAdapter mAdapter;
	private UARTProfile mProfile;
	private long mConfigurationId;

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
					Toast.makeText(UARTCommandsActivity.this, message, Toast.LENGTH_SHORT).show();
					// TODO error handing
					break;
				}
				case UARTProfile.BROADCAST_DATA_RECEIVED: {
					// Here we could have shown the incoming message somehow.
					// However, notifications on TX characteristics are not enabled so this does not have to be implemented.
//					final String message = intent.getStringExtra(UARTProfile.EXTRA_DATA);
//					Toast.makeText(UARTCommandsActivity.this, message, Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}
	};

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName name, final IBinder service) {
			final BleProfileService.LocalBinder binder = (BleProfileService.LocalBinder) service;
			mProfile = (UARTProfile) binder.getProfile();
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			mProfile = null;
		}
	};

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grid_pager);

		final Intent intent = getIntent();
		final UartConfiguration configuration = intent.getParcelableExtra(CONFIGURATION);
		mConfigurationId = configuration.getId();

		// Check if the WEAR device is connected to the UART device itself, or by the phone.
		// Binding will fail if we are using phone as proxy as the service has not been started before.
		final Intent service = new Intent(this, BleProfileService.class);
		bindService(service, mServiceConnection, 0);

		// Set up tht grid
		final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
		pager.setAdapter(mAdapter = new UARTCommandsAdapter(configuration, this));

		final DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
		dotsPageIndicator.setPager(pager);

		// Configure Google API client
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

		// Register the broadcast receiver that will listen for events from the device
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BleProfileService.BROADCAST_CONNECTION_STATE);
		filter.addAction(BleProfileService.BROADCAST_ERROR);
		filter.addAction(UARTProfile.BROADCAST_DATA_RECEIVED);
		LocalBroadcastManager.getInstance(this).registerReceiver(mServiceBroadcastReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mGoogleApiClient.unregisterConnectionCallbacks(this);
		mGoogleApiClient.unregisterConnectionFailedListener(this);
		mGoogleApiClient = null;

		// unbind if we were binded to the service.
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
	}

	@Override
	public void onConnectionSuspended(final int cause) {
		finish();
	}

	@Override
	public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
		finish();
	}

	@Override
	public void onDataChanged(final DataEventBuffer dataEventBuffer) {
		for (final DataEvent event : dataEventBuffer) {
			final DataItem item = event.getDataItem();
			final long id = ContentUris.parseId(item.getUri());

			// Update the configuration only if ID matches
			if (id != mConfigurationId)
				continue;

			// Configuration added or edited
			if (event.getType() == DataEvent.TYPE_CHANGED) {
				final DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
				final UartConfiguration configuration = new UartConfiguration(dataMap, id);

				// Update UI on UI thread
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mAdapter.setConfiguration(configuration);
					}
				});
			} else if (event.getType() == DataEvent.TYPE_DELETED) {
				// Configuration removed

				// Update UI on UI thread
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mAdapter.setConfiguration(null);
					}
				});
			}
		}
	}

	@Override
	public void onMessageReceived(final MessageEvent messageEvent) {
		// If the activity is binded to service it means that it has connected directly to the device. We ignore messages from the handheld.
		if (mProfile != null)
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
	public void onCommandSelected(final Command command) {
		// Send command to handheld if the watch is not connected directly to the UART device.
		final Command.Eol eol = command.getEol();
		String text = command.getCommand();
		switch (eol) {
			case CR_LF:
				text = text.replaceAll("\n", "\r\n");
				break;
			case CR:
				text = text.replaceAll("\n", "\r");
				break;
		}

		if (mProfile != null)
			mProfile.send(text);
		else
			sendMessageToHandheld(this, text);
	}

	/**
	 * Sends the given command to the handheld.
	 *
	 * @param command the message
	 */
	private void sendMessageToHandheld(final @NonNull Context context, final @NonNull String command) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final GoogleApiClient client = new GoogleApiClient.Builder(context)
						.addApi(Wearable.API)
						.build();
				client.blockingConnect();

				final NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
				for (Node node : nodes.getNodes()) {
					final MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(client, node.getId(), Constants.UART.COMMAND, command.getBytes()).await();
					if (!result.getStatus().isSuccess()) {
						Log.w(TAG, "Failed to send " + Constants.UART.COMMAND + " to " + node.getDisplayName());
					}
				}
				client.disconnect();
			}
		}).start();
	}
}
