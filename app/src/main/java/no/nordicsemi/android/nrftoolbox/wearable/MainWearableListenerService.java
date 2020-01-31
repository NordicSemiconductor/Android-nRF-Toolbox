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

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import no.nordicsemi.android.nrftoolbox.uart.UARTService;
import no.nordicsemi.android.nrftoolbox.wearable.common.Constants;

/**
 * The main listener for messages from Wearable devices. There may be only one such service per application so it has to handle messages from all profiles.
 */
public class MainWearableListenerService extends WearableListenerService {

	@Override
	public void onMessageReceived(final MessageEvent messageEvent) {
		switch (messageEvent.getPath()) {
			case Constants.ACTION_DISCONNECT: {
				// A disconnect message was sent. The information which profile should be disconnected is in the data.
				final String profile = new String(messageEvent.getData());

				//noinspection SwitchStatementWithTooFewBranches
				switch (profile) {
					// Currently only UART profile has Wear support
					case Constants.UART.PROFILE: {
						final Intent disconnectIntent = new Intent(UARTService.ACTION_DISCONNECT);
						disconnectIntent.putExtra(UARTService.EXTRA_SOURCE, UARTService.SOURCE_WEARABLE);
						sendBroadcast(disconnectIntent);
						break;
					}
				}
				break;
			}
			case Constants.UART.COMMAND: {
				final String command = new String(messageEvent.getData());

				final Intent intent = new Intent(UARTService.ACTION_SEND);
				intent.putExtra(UARTService.EXTRA_SOURCE, UARTService.SOURCE_WEARABLE);
				intent.putExtra(Intent.EXTRA_TEXT, command);
				sendBroadcast(intent);
			}
			default:
				super.onMessageReceived(messageEvent);
				break;
		}
	}
}
