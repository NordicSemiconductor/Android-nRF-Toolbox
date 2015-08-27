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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.uart.domain.Command;
import no.nordicsemi.android.nrftoolbox.uart.domain.UartConfiguration;

public class UARTButtonAdapter extends BaseAdapter {
	private UartConfiguration mConfiguration;
	private boolean mEditMode;

	public UARTButtonAdapter(final UartConfiguration configuration) {
		mConfiguration = configuration;
	}

	public void setEditMode(final boolean editMode) {
		mEditMode = editMode;
		notifyDataSetChanged();
	}

	public void setConfiguration(final UartConfiguration configuration) {
		mConfiguration = configuration;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mConfiguration != null ? mConfiguration.getCommands().length : 0;
	}

	@Override
	public Object getItem(final int position) {
		return mConfiguration.getCommands()[position];
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		final Command command = (Command) getItem(position);
		return mEditMode || (command != null && command.isActive());
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			view = inflater.inflate(R.layout.feature_uart_button, parent, false);
		}
		view.setEnabled(isEnabled(position));
		view.setActivated(mEditMode);

		// Update image
		final Command command = (Command) getItem(position);
		final ImageView image = (ImageView) view;
		final boolean active = command != null && command.isActive();
		if (active) {
			final int icon = command.getIconIndex();
			image.setImageResource(R.drawable.uart_button);
			image.setImageLevel(icon);
		} else
			image.setImageDrawable(null);

		return view;
	}
}
