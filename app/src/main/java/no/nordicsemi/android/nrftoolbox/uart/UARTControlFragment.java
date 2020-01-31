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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.uart.domain.Command;
import no.nordicsemi.android.nrftoolbox.uart.domain.UartConfiguration;

public class UARTControlFragment extends Fragment implements GridView.OnItemClickListener, UARTActivity.ConfigurationListener {
	private final static String TAG = "UARTControlFragment";
	private final static String SIS_EDIT_MODE = "sis_edit_mode";

	private UartConfiguration configuration;
	private UARTButtonAdapter adapter;
	private boolean editMode;

	@Override
	public void onAttach(@NonNull final Context context) {
		super.onAttach(context);

		try {
			((UARTActivity)context).setConfigurationListener(this);
		} catch (final ClassCastException e) {
			Log.e(TAG, "The parent activity must implement EditModeListener");
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			editMode = savedInstanceState.getBoolean(SIS_EDIT_MODE);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		((UARTActivity)requireActivity()).setConfigurationListener(null);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putBoolean(SIS_EDIT_MODE, editMode);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_feature_uart_control, container, false);

		final GridView grid = view.findViewById(R.id.grid);
		grid.setAdapter(adapter = new UARTButtonAdapter(configuration));
		grid.setOnItemClickListener(this);
		adapter.setEditMode(editMode);

		return view;
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		if (editMode) {
			Command command = configuration.getCommands()[position];
			if (command == null)
				configuration.getCommands()[position] = command = new Command();
			final UARTEditDialog dialog = UARTEditDialog.getInstance(position, command);
			dialog.show(getChildFragmentManager(), null);
		} else {
			final Command command = (Command)adapter.getItem(position);
			final Command.Eol eol = command.getEol();
			String text = command.getCommand();
			if (text == null)
				text = "";
			switch (eol) {
				case CR_LF:
					text = text.replaceAll("\n", "\r\n");
					break;
				case CR:
					text = text.replaceAll("\n", "\r");
					break;
			}
			final UARTInterface uart = (UARTInterface) requireActivity();
			uart.send(text);
		}
	}

	@Override
	public void onConfigurationModified() {
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onConfigurationChanged(@NonNull final UartConfiguration configuration) {
		this.configuration = configuration;
		adapter.setConfiguration(configuration);
	}

	@Override
	public void setEditMode(final boolean editMode) {
		this.editMode = editMode;
		adapter.setEditMode(editMode);
	}
}
