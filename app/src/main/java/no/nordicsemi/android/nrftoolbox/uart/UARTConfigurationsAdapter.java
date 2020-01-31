/*************************************************************************************************************************************************
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
 ************************************************************************************************************************************************/

package no.nordicsemi.android.nrftoolbox.uart;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.nrftoolbox.R;

public class UARTConfigurationsAdapter extends CursorAdapter {
	final Context context;
	final ActionListener listener;

	public interface ActionListener {
		void onNewConfigurationClick();
		void onImportClick();
	}

	public UARTConfigurationsAdapter(@NonNull final Context context, final ActionListener listener, final Cursor c) {
		super(context, c, 0);
		this.context = context;
		this.listener = listener;
	}

	@Override
	public int getCount() {
		return super.getCount() + 1; // One for buttons at the top
	}

	@Override
	public boolean isEmpty() {
		return super.getCount() == 0;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public long getItemId(final int position) {
		if (position > 0)
			return super.getItemId(position - 1);
		return 0;
	}

	public int getItemPosition(final long id) {
		final Cursor cursor = getCursor();
		if (cursor == null)
			return 1;

		if (cursor.moveToFirst())
			do {
				if (cursor.getLong(0 /* _ID */) == id)
					return cursor.getPosition() + 1;
			} while (cursor.moveToNext());
		return 1; // should never happen
	}

	@Override
	public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
		if (position == 0) {
			// This empty view should never be visible. Only positions 1+ are valid. Position 0 is reserved for action buttons.
			// It is only created temporally when activity is created.
			return LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
		}
		return super.getView(position - 1, convertView, parent);
	}

	@Override
	public View getDropDownView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
		if (position == 0) {
			return newToolbarView(context, parent);
		}
		if (convertView instanceof ViewGroup)
			return super.getDropDownView(position - 1, null, parent);
		return super.getDropDownView(position - 1, convertView, parent);
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		return LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false);
	}

	@Override
	public View newDropDownView(final Context context, final Cursor cursor, final ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.feature_uart_dropdown_item, parent, false);
	}

	public View newToolbarView(final Context context, final ViewGroup parent) {
		final View view = LayoutInflater.from(context).inflate(R.layout.feature_uart_dropdown_title, parent, false);
		view.findViewById(R.id.action_add).setOnClickListener(v -> listener.onNewConfigurationClick());
		view.findViewById(R.id.action_import).setOnClickListener(v -> listener.onImportClick());
		return view;
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final String name = cursor.getString(1 /* NAME */);
		((TextView) view).setText(name);
	}
}
