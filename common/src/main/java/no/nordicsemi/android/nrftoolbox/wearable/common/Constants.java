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

package no.nordicsemi.android.nrftoolbox.wearable.common;

/**
 * Constants used for exchanging data and messages between handheld and Android Wear devices.
 */
public final class Constants {
	/** Base path for all messages between the handheld and wearable. */
	private static final String BASE_PATH =  "/nrftoolbox";

	/** Action sent from a wearable to disconnect from a device. It must have the profile name set as data. */
	public static final String ACTION_DISCONNECT = BASE_PATH + "/disconnect";

	/**
	 * Constants for the UART profile.
	 */
	public static final class UART {
		/** The profile name. */
		public static final String PROFILE = "uart";

		/** Base path for UART messages between the handheld and wearable. */
		private static final String PROFILE_PATH =  BASE_PATH + "/uart";

		/** An UART device is connected. */
		public static final String DEVICE_CONNECTED = PROFILE_PATH + "/connected";
		/** An UART device is disconnected. */
		public static final String DEVICE_DISCONNECTED = PROFILE_PATH + "/disconnected";
		/** An UART device is disconnected due to a link loss. */
		public static final String DEVICE_LINKLOSS = PROFILE_PATH + "/link_loss";
		/** Path used for syncing UART configurations. */
		public static final String CONFIGURATIONS = PROFILE_PATH + "/configurations";
		/** An action with a command was clicked. */
		public static final String COMMAND = PROFILE_PATH + "/command";

		public static final class Configuration {
			public static final String NAME = "name";
			public static final String COMMANDS = "commands";

			public static final class Command {
				public static final String ICON_ID = "icon_id";
				public static final String MESSAGE = "message";
				public static final String EOL = "eol";
			}
		}
	}
}
