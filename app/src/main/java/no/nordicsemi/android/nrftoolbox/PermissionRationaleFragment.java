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
package no.nordicsemi.android.nrftoolbox;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class PermissionRationaleFragment extends DialogFragment {
	private static final String ARG_PERMISSION = "ARG_PERMISSION";
	private static final String ARG_TEXT = "ARG_TEXT";

	private PermissionDialogListener mListener;

	public interface PermissionDialogListener {
		public void onRequestPermission(final String permission);
	}

	@Override
	public void onAttach(final Context context) {
		super.onAttach(context);

		if (context instanceof PermissionDialogListener) {
			mListener = (PermissionDialogListener) context;
		} else {
			throw new IllegalArgumentException("The parent activity must impelemnt PermissionDialogListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public static PermissionRationaleFragment getInstance(final int aboutResId, final String permission) {
		final PermissionRationaleFragment fragment = new PermissionRationaleFragment();

		final Bundle args = new Bundle();
		args.putInt(ARG_TEXT, aboutResId);
		args.putString(ARG_PERMISSION, permission);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
    @NonNull
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Bundle args = getArguments();
		final StringBuilder text = new StringBuilder(getString(args.getInt(ARG_TEXT)));
		return new AlertDialog.Builder(getActivity()).setTitle(R.string.permission_title).setMessage(text)
				.setNegativeButton(R.string.cancel, null)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						mListener.onRequestPermission(args.getString(ARG_PERMISSION));
					}
				}).create();
	}
}
