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

import android.support.wearable.view.CircularButton;
import android.support.wearable.view.GridPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.uart.domain.Command;
import no.nordicsemi.android.nrftoolbox.uart.domain.UartConfiguration;

class UARTCommandsAdapter extends GridPagerAdapter {
	private final OnCommandSelectedListener listener;
	private UartConfiguration configuration;

	public interface OnCommandSelectedListener {
		void onCommandSelected(final Command command);
	}

	UARTCommandsAdapter(final UartConfiguration configuration, final OnCommandSelectedListener listener) {
		this.configuration = configuration;
		this.listener = listener;
	}

	void setConfiguration(final UartConfiguration configuration) {
		// Configuration is null when it has been deleted on the handheld
		this.configuration = configuration;
		notifyDataSetChanged();
	}

	@Override
	public int getRowCount() {
		return 1;
	}

	@Override
	public int getColumnCount(final int row) {
		final int count = configuration != null ? configuration.getCommands().length : 0;
		return count > 0 ? count : 1; // Empty view
	}

	@Override
	public Object instantiateItem(final ViewGroup viewGroup, final int row, final int column) {
		final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.action_item, viewGroup, false);
		viewGroup.addView(view);

		final Command[] commands = configuration != null ? configuration.getCommands() : null;
		if (commands != null && commands.length > 0) {
			final Command command = commands[column];

			final CircularButton icon = view.findViewById(R.id.icon);
			icon.getImageDrawable().setLevel(command.getIconIndex());
			icon.setOnClickListener(v -> listener.onCommandSelected(command));
		} else {
			// Hide the icon
			view.findViewById(R.id.icon).setVisibility(View.GONE);

			// and show the message
			final TextView emptyView = view.findViewById(R.id.empty);
			emptyView.setVisibility(View.VISIBLE);
			if (commands == null)
				emptyView.setText(R.string.configuration_deleted);
		}
		return view;
	}

	@Override
	public void destroyItem(final ViewGroup viewGroup, final int row, final int column, final Object object) {
		final View view = (View) object;
		viewGroup.removeView(view);
	}

	@Override
	public boolean isViewFromObject(final View view, final Object object) {
		return view == object;
	}
}
