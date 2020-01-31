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

package no.nordicsemi.android.nrftoolbox.uart.wearable;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

import no.nordicsemi.android.nrftoolbox.uart.domain.Command;
import no.nordicsemi.android.nrftoolbox.uart.domain.UartConfiguration;
import no.nordicsemi.android.nrftoolbox.wearable.common.Constants;

public class UARTConfigurationSynchronizer {
	private static final String WEAR_URI_PREFIX = "wear:"; // no / at the end as the path already has it

	private static UARTConfigurationSynchronizer instance;
	private GoogleApiClient googleApiClient;

	/**
	 * Initializes the synchronizer.
	 * @param context the activity context
	 * @param listener the connection callbacks listener
	 */
	public static UARTConfigurationSynchronizer from(final Context context, final GoogleApiClient.ConnectionCallbacks listener) {
		if (instance == null)
			instance = new UARTConfigurationSynchronizer();

		instance.init(context, listener);
		return instance;
	}

	private UARTConfigurationSynchronizer() {
		// private constructor
	}

	private void init(final Context context, final GoogleApiClient.ConnectionCallbacks listener) {
		if (googleApiClient != null)
			return;

		googleApiClient = new GoogleApiClient.Builder(context)
				.addApiIfAvailable(Wearable.API)
				.addConnectionCallbacks(listener)
				.build();
		googleApiClient.connect();
	}

	/**
	 * Closes the synchronizer.
	 */
	public void close() {
		if (googleApiClient != null)
			googleApiClient.disconnect();
		googleApiClient = null;
	}

	/**
	 * Returns true if Wearable API has been connected.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean hasConnectedApi() {
		return googleApiClient != null && googleApiClient.isConnected() && googleApiClient.hasConnectedApi(Wearable.API);
	}

	/**
	 * Synchronizes the UART configurations between handheld and wearables.
	 * Call this when configuration has been created or altered.
	 * @return pending result
	 */
	public PendingResult<DataApi.DataItemResult> onConfigurationAddedOrEdited(final long id, final UartConfiguration configuration) {
		if (!hasConnectedApi())
			return null;

		final PutDataMapRequest mapRequest = PutDataMapRequest.create(Constants.UART.CONFIGURATIONS + "/" + id);
		final DataMap map = mapRequest.getDataMap();
		map.putString(Constants.UART.Configuration.NAME, configuration.getName());
		final ArrayList<DataMap> commands = new ArrayList<>(UartConfiguration.COMMANDS_COUNT);
		for (Command command : configuration.getCommands()) {
			if (command != null && command.isActive()) {
				final DataMap item = new DataMap();
				item.putInt(Constants.UART.Configuration.Command.ICON_ID, command.getIconIndex());
				item.putString(Constants.UART.Configuration.Command.MESSAGE, command.getCommand());
				item.putInt(Constants.UART.Configuration.Command.EOL, command.getEolIndex());
				commands.add(item);
			}
		}
		map.putDataMapArrayList(Constants.UART.Configuration.COMMANDS, commands);
		final PutDataRequest request = mapRequest.asPutDataRequest();
		return Wearable.DataApi.putDataItem(googleApiClient, request);
	}

	/**
	 * Synchronizes the UART configurations between handheld and wearables.
	 * Call this when configuration has been deleted.
	 * @return pending result
	 */
	@SuppressWarnings("UnusedReturnValue")
	public PendingResult<DataApi.DeleteDataItemsResult> onConfigurationDeleted(final long id) {
		if (!hasConnectedApi())
			return null;
		return Wearable.DataApi.deleteDataItems(googleApiClient, id2Uri(id));
	}

	/**
	 * Creates URI without nodeId.
	 * @param id the configuration id in the database
	 * @return Uri that may be used to delete the associated DataMap.
	 */
	private Uri id2Uri(final long id) {
		return Uri.parse(WEAR_URI_PREFIX + Constants.UART.CONFIGURATIONS + "/" + id);
	}
}
