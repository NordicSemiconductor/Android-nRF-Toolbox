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

import no.nordicsemi.android.nrftoolbox.wearable.common.Constants;

public class Command implements Parcelable {
	public enum Icon {
		LEFT(0),
		UP(1),
		RIGHT(2),
		DOWN(3),
		SETTINGS(4),
		REW(5),
		PLAY(6),
		PAUSE(7),
		STOP(8),
		FWD(9),
		INFO(10),
		NUMBER_1(11),
		NUMBER_2(12),
		NUMBER_3(13),
		NUMBER_4(14),
		NUMBER_5(15),
		NUMBER_6(16),
		NUMBER_7(17),
		NUMBER_8(18),
		NUMBER_9(19);

		public int index;

		Icon(final int index) {
			this.index = index;
		}
	}

	public enum Eol {
		LF(0),
		CR(1),
		CR_LF(2);

		public final int index;

		Eol(final int index) {
			this.index = index;
		}
	}

	private Eol eol = Eol.LF;
	private Icon icon = Icon.LEFT;
	private String command;

	/* package */ Command(final DataMap dataMap) {
		icon = Icon.values()[dataMap.getInt(Constants.UART.Configuration.Command.ICON_ID)];
		command = dataMap.getString(Constants.UART.Configuration.Command.MESSAGE);
		eol = Eol.values()[dataMap.getInt(Constants.UART.Configuration.Command.EOL)];
	}

	private Command(final Parcel in) {
		icon = (Icon) in.readSerializable();
		command = in.readString();
		eol = (Eol) in.readSerializable();
	}

	/**
	 * Sets the command.
	 * @param command the command that will be sent to UART device
	 */
	/* package */ void setCommand(final String command) {
		this.command = command;
	}

	/**
	 * Sets the new line type.
	 * @param eol end of line terminator
	 */
	/* package */ void setEol(final int eol) {
		this.eol = Eol.values()[eol];
	}

	/**
	 * Sets the icon index.
	 * @param index index of the icon.
	 */
	/* package */ void setIconIndex(final int index) {
		this.icon = Icon.values()[index];
	}

	/**
	 * Returns the command that will be sent to UART device.
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Returns the new line type.
	 * @return end of line terminator
	 */
	public Eol getEol() {
		return eol;
	}

	/**
	 * Returns the icon index.
	 * @return the icon index
	 */
	public int getIconIndex() {
		return icon.index;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<Command> CREATOR = new Parcelable.Creator<Command>() {
		@Override
		public Command createFromParcel(final Parcel in) {
			return new Command(in);
		}

		@Override
		public Command[] newArray(final int size) {
			return new Command[size];
		}
	};

	@Override
	public void writeToParcel(final Parcel dest, int flags) {
		dest.writeSerializable(icon);
		dest.writeString(command);
		dest.writeSerializable(eol);
	}
}
