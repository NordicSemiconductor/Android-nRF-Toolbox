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

package no.nordicsemi.android.nrftoolbox.uart.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.wearable.DataMap;

import java.util.ArrayList;

import no.nordicsemi.android.nrftoolbox.wearable.common.Constants;

public class UartConfiguration implements Parcelable {
	private long id;
	private String name;
	private Command[] commands;

	public UartConfiguration(final DataMap dataMap, final long id) {
		name = dataMap.getString(Constants.UART.Configuration.NAME);

		final ArrayList<DataMap> maps = dataMap.getDataMapArrayList(Constants.UART.Configuration.COMMANDS);
		commands = new Command[maps.size()];
		for (int i = 0; i < maps.size(); ++i) {
			commands[i] = new Command(maps.get(i));
		}

		this.id = id;
	}

	private UartConfiguration(final Parcel in) {
		id = in.readLong();
		name = in.readString();
		commands = in.createTypedArray(Command.CREATOR);
	}

	/**
	 * Returns the configuration ID.
	 * @return the ID of the configuration in the handheld's database.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Returns the field name
	 *
	 * @return optional name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the array of commands. There is always 9 of them.
	 * @return the commands array
	 */
	public Command[] getCommands() {
		return commands;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<UartConfiguration> CREATOR = new Parcelable.Creator<UartConfiguration>() {
		@Override
		public UartConfiguration createFromParcel(final Parcel in) {
			return new UartConfiguration(in);
		}

		@Override
		public UartConfiguration[] newArray(final int size) {
			return new UartConfiguration[size];
		}
	};

	@Override
	public void writeToParcel(final Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeTypedArray(commands, 0);
	}
}
