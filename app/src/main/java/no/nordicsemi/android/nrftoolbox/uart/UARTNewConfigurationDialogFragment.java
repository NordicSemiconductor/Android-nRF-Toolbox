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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import no.nordicsemi.android.nrftoolbox.R;

public class UARTNewConfigurationDialogFragment extends DialogFragment implements View.OnClickListener {
	private static final String NAME = "name";
	private static final String DUPLICATE = "duplicate";

	private EditText editText;

	private NewConfigurationDialogListener listener;

	public interface NewConfigurationDialogListener {
		/**
		 * Creates a new configuration with given name.
		 * @param name the name
		 * @param duplicate true if configuration is to be duplicated
		 */
		void onNewConfiguration(final String name, final boolean duplicate);

		/**
		 * Renames the current configuration with given name.
		 * @param newName the new name
		 */
		void onRenameConfiguration(final String newName);
	}

	@Override
	public void onAttach(@NonNull final Context context) {
		super.onAttach(context);

		if (context instanceof NewConfigurationDialogListener) {
			listener = (NewConfigurationDialogListener) context;
		} else {
			throw new IllegalArgumentException("The parent activity must implement NewConfigurationDialogListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	public static DialogFragment getInstance(final String name, final boolean duplicate) {
		final DialogFragment dialog = new UARTNewConfigurationDialogFragment();

		final Bundle args = new Bundle();
		args.putString(NAME, name);
		args.putBoolean(DUPLICATE, duplicate);
		dialog.setArguments(args);

		return dialog;
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Bundle args = requireArguments();
		final String oldName = args.getString(NAME);
		final boolean duplicate = args.getBoolean(DUPLICATE);
		final int titleResId = duplicate || oldName == null ? R.string.uart_new_configuration_title : R.string.uart_rename_configuration_title;

		final LayoutInflater inflater = LayoutInflater.from(requireContext());
		final View view = inflater.inflate(R.layout.feature_uart_dialog_new_configuration, null);
		final EditText editText = this.editText = view.findViewById(R.id.name);
		editText.setText(args.getString(NAME));
		final View actionClear = view.findViewById(R.id.action_clear);
		actionClear.setOnClickListener(v -> editText.setText(null));

		final AlertDialog dialog = new AlertDialog.Builder(requireContext())
				.setTitle(titleResId)
				.setView(view)
				.setNegativeButton(R.string.cancel, null)
				.setPositiveButton(R.string.ok, null)
				.setCancelable(false)
				.show(); // this must be show() or the getButton() below will return null.

		final Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		okButton.setOnClickListener(this);

		return dialog;
	}

	@Override
	public void onClick(final View v) {
		final String newName = editText.getText().toString().trim();
		if (TextUtils.isEmpty(newName)) {
			editText.setError(getString(R.string.uart_empty_name_error));
			return;
		}

		final Bundle args = requireArguments();
		final String oldName = args.getString(NAME);
		final boolean duplicate = args.getBoolean(DUPLICATE);

		if (duplicate || TextUtils.isEmpty(oldName))
			listener.onNewConfiguration(newName, duplicate);
		else {
			listener.onRenameConfiguration(newName);
		}
		dismiss();
	}
}
