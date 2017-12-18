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

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioGroup;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.uart.domain.Command;

public class UARTEditDialog extends DialogFragment implements View.OnClickListener, GridView.OnItemClickListener {
	private final static String ARG_INDEX = "index";
	private final static String ARG_COMMAND = "command";
	private final static String ARG_EOL = "eol";
	private final static String ARG_ICON_INDEX = "iconIndex";
	private int mActiveIcon;

	private EditText mField;
	private CheckBox mActiveCheckBox;
	private RadioGroup mEOLGroup;
	private IconAdapter mIconAdapter;

	public static UARTEditDialog getInstance(final int index, final Command command) {
		final UARTEditDialog fragment = new UARTEditDialog();

		final Bundle args = new Bundle();
		args.putInt(ARG_INDEX, index);
		args.putString(ARG_COMMAND, command.getCommand());
		args.putInt(ARG_EOL, command.getEol().index);
		args.putInt(ARG_ICON_INDEX, command.getIconIndex());
		fragment.setArguments(args);

		return fragment;
	}

	@NonNull
    @Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final LayoutInflater inflater = LayoutInflater.from(getActivity());

		// Read button configuration
		final Bundle args = getArguments();
		final int index = args.getInt(ARG_INDEX);
		final String command = args.getString(ARG_COMMAND);
		final int eol = args.getInt(ARG_EOL);
		final int iconIndex = args.getInt(ARG_ICON_INDEX);
		final boolean active = true; // change to active by default
		mActiveIcon = iconIndex;

		// Create view
		final View view = inflater.inflate(R.layout.feature_uart_dialog_edit, null);
		final EditText field = mField = view.findViewById(R.id.field);
		final GridView grid = view.findViewById(R.id.grid);
		final CheckBox checkBox = mActiveCheckBox = view.findViewById(R.id.active);
		checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
			field.setEnabled(isChecked);
			grid.setEnabled(isChecked);
			if (mIconAdapter != null)
				mIconAdapter.notifyDataSetChanged();
		});

		final RadioGroup eolGroup = mEOLGroup = view.findViewById(R.id.uart_eol);
		switch (Command.Eol.values()[eol]) {
			case CR_LF:
				eolGroup.check(R.id.uart_eol_cr_lf);
				break;
			case CR:
				eolGroup.check(R.id.uart_eol_cr);
				break;
			case LF:
			default:
				eolGroup.check(R.id.uart_eol_lf);
				break;
		}

		field.setText(command);
		field.setEnabled(active);
		checkBox.setChecked(active);
		grid.setOnItemClickListener(this);
		grid.setEnabled(active);
		grid.setAdapter(mIconAdapter = new IconAdapter());

		// As we want to have some validation we can't user the DialogInterface.OnClickListener as it's always dismissing the dialog.
		final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setCancelable(false).setTitle(R.string.uart_edit_title).setPositiveButton(R.string.ok, null)
				.setNegativeButton(R.string.cancel, null).setView(view).show();
		final Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		okButton.setOnClickListener(this);
		return dialog;
	}

	@Override
	public void onClick(final View v) {
		final boolean active = mActiveCheckBox.isChecked();
		final String command = mField.getText().toString();
		int eol;

		switch (mEOLGroup.getCheckedRadioButtonId()) {
			case R.id.uart_eol_cr_lf:
				eol = Command.Eol.CR_LF.index;
				break;
			case R.id.uart_eol_cr:
				eol = Command.Eol.CR.index;
				break;
			case R.id.uart_eol_lf:
			default:
				eol = Command.Eol.LF.index;
				break;
		}

		// Save values
		final Bundle args = getArguments();
		final int index = args.getInt(ARG_INDEX);

		dismiss();
		final UARTActivity parent = (UARTActivity) getActivity();
		parent.onCommandChanged(index, command, active, eol, mActiveIcon);
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		mActiveIcon = position;
		mIconAdapter.notifyDataSetChanged();
	}

	private class IconAdapter extends BaseAdapter {
		private final int SIZE = 20;

		@Override
		public int getCount() {
			return SIZE;
		}

		@Override
		public Object getItem(final int position) {
			return position;
		}

		@Override
		public long getItemId(final int position) {
			return position;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = LayoutInflater.from(getActivity()).inflate(R.layout.feature_uart_dialog_edit_icon, parent, false);
			}
			final ImageView image = (ImageView) view;
			image.setImageLevel(position);
			image.setActivated(position == mActiveIcon && mActiveCheckBox.isChecked());
			return view;
		}
	}
}
