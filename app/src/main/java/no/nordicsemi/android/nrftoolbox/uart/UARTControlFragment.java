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

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import no.nordicsemi.android.nrftoolbox.R;

public class UARTControlFragment extends Fragment implements GridView.OnItemClickListener {
	private final static String TAG = "UARTControlFragment";
	private final static String SIS_EDIT_MODE = "sis_edit_mode";

	private ControlFragmentListener mListener;
	private SharedPreferences mPreferences;
	private UARTButtonAdapter mAdapter;
	private boolean mEditMode;

	public static interface ControlFragmentListener {
		public void setEditMode(final boolean editMode);
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);

		try {
			mListener = (ControlFragmentListener) activity;
		} catch (final ClassCastException e) {
			Log.e(TAG, "The parent activity must implement EditModeListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

		if (savedInstanceState != null) {
			mEditMode = savedInstanceState.getBoolean(SIS_EDIT_MODE);
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putBoolean(SIS_EDIT_MODE, mEditMode);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_feature_uart_control, container, false);

		final GridView grid = (GridView) view.findViewById(R.id.grid);
		grid.setAdapter(mAdapter = new UARTButtonAdapter(getActivity()));
		grid.setOnItemClickListener(this);
		mAdapter.setEditMode(mEditMode);

		setHasOptionsMenu(true);
		return view;
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		if (mEditMode) {
			final UARTEditDialog dialog = UARTEditDialog.getInstance(position);
			dialog.show(getChildFragmentManager(), null);
		} else {
			final UARTInterface uart = (UARTInterface) getActivity();
			uart.send(mPreferences.getString(UARTButtonAdapter.PREFS_BUTTON_COMMAND + position, ""));
		}
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(mEditMode ? R.menu.uart_menu_config : R.menu.uart_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		final int itemId = item.getItemId();
		switch (itemId) {
		case R.id.action_configure:
			setEditMode(!mEditMode);
			return true;
		}
		return false;
	}

	public void setEditMode(final boolean editMode) {
		mEditMode = editMode;
		mAdapter.setEditMode(mEditMode);
		getActivity().invalidateOptionsMenu();
		mListener.setEditMode(mEditMode);
	}

	public void onConfigurationChanged() {
		mAdapter.notifyDataSetChanged();
	}
}
