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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import no.nordicsemi.android.nrftoolbox.R;

public class UARTButtonAdapter extends BaseAdapter {
	public final static String PREFS_BUTTON_ENABLED = "prefs_uart_enabled_";
	public final static String PREFS_BUTTON_COMMAND = "prefs_uart_command_";
	public final static String PREFS_BUTTON_ICON = "prefs_uart_icon_";

	private final SharedPreferences mPreferences;
	private final int[] mIcons;
	private final boolean[] mEnableFlags;
	private boolean mEditMode;

	public UARTButtonAdapter(final Context context) {
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		mIcons = new int[9];
		mEnableFlags = new boolean[9];
	}

	public void setEditMode(final boolean editMode) {
		mEditMode = editMode;
		notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		final SharedPreferences preferences = mPreferences;
		for (int i = 0; i < mIcons.length; ++i) {
			mIcons[i] = preferences.getInt(PREFS_BUTTON_ICON + i, -1);
			mEnableFlags[i] = preferences.getBoolean(PREFS_BUTTON_ENABLED + i, false);
		}
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mIcons.length;
	}

	@Override
	public Object getItem(final int position) {
		return mIcons[position];
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
		return mEditMode || mEnableFlags[position];
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
		final ImageView image = (ImageView) view;
		final int icon = mIcons[position];
		if (mEnableFlags[position] && icon != -1) {
			image.setImageResource(R.drawable.uart_button);
			image.setImageLevel(icon);
		} else
			image.setImageDrawable(null);

		return view;
	}
}
