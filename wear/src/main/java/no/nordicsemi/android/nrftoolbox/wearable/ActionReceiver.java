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

package no.nordicsemi.android.nrftoolbox.wearable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import no.nordicsemi.android.nrftoolbox.wearable.common.Constants;

public class ActionReceiver extends BroadcastReceiver {
	private static final String TAG = "ActionReceiver";

	public static final String ACTION_DISCONNECT = "no.nordicsemi.android.nrftoolbox.ACTION_DISCONNECT";
	public static final String EXTRA_DATA = "no.nordicsemi.android.nrftoolbox.EXTRA_DATA";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		switch (intent.getAction()) {
			case ACTION_DISCONNECT: {
				final String profile = intent.getStringExtra(EXTRA_DATA);
				sendMessageToHandheld(context, Constants.ACTION_DISCONNECT, profile);
				break;
			}
		}
	}

	/**
	 * Sends the given message to the handheld.
	 * @param path message path
	 * @param message the message
	 */
	private void sendMessageToHandheld(final @NonNull Context context, final @NonNull String path, final @NonNull String message) {
		new Thread(() -> {
			final GoogleApiClient client = new GoogleApiClient.Builder(context)
					.addApi(Wearable.API)
					.build();
			client.blockingConnect();

			final NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
			for(Node node : nodes.getNodes()) {
				final MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(client, node.getId(), path, message.getBytes()).await();
				if (!result.getStatus().isSuccess()){
					Log.w(TAG, "Failed to send " + path + " to " + node.getDisplayName());
				}
			}
			client.disconnect();
		}).start();
	}
}
