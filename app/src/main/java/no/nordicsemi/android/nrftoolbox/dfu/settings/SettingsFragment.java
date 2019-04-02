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
package no.nordicsemi.android.nrftoolbox.dfu.settings;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuSettingsConstants;
import no.nordicsemi.android.nrftoolbox.R;

public class SettingsFragment extends PreferenceFragmentCompat implements DfuSettingsConstants, SharedPreferences.OnSharedPreferenceChangeListener {
	public static final String SETTINGS_KEEP_BOND = "settings_keep_bond";

	@Override
	public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
		addPreferencesFromResource(R.xml.settings_dfu);

		// set initial values
		updateNumberOfPacketsSummary();
		updateMBRSize();
	}

	@Override
	public void onResume() {
		super.onResume();

		// attach the preference change listener. It will update the summary below interval preference
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		// unregister listener
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		final SharedPreferences preferences = getPreferenceManager().getSharedPreferences();

		if (SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED.equals(key)) {
			final boolean disabled = !preferences.getBoolean(SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED, true);
			if (disabled && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
				new AlertDialog.Builder(requireContext()).setMessage(R.string.dfu_settings_dfu_number_of_packets_info).setTitle(R.string.dfu_settings_dfu_information)
						.setPositiveButton(R.string.ok, null).show();
			}
		} else if (SETTINGS_NUMBER_OF_PACKETS.equals(key)) {
			updateNumberOfPacketsSummary();
		} else if (SETTINGS_MBR_SIZE.equals(key)) {
			updateMBRSize();
		} else if (SETTINGS_ASSUME_DFU_NODE.equals(key) && sharedPreferences.getBoolean(key, false)) {
			new AlertDialog.Builder(requireContext()).setMessage(R.string.dfu_settings_dfu_assume_dfu_mode_info).setTitle(R.string.dfu_settings_dfu_information)
					.setPositiveButton(R.string.ok, null)
					.show();
		}
	}

	private void updateNumberOfPacketsSummary() {
		final PreferenceScreen screen = getPreferenceScreen();
		final SharedPreferences preferences = getPreferenceManager().getSharedPreferences();

        String value = preferences.getString(SETTINGS_NUMBER_OF_PACKETS, String.valueOf(SETTINGS_NUMBER_OF_PACKETS_DEFAULT));
        // Security check
        if (TextUtils.isEmpty(value)) {
            value = String.valueOf(SETTINGS_NUMBER_OF_PACKETS_DEFAULT);
            preferences.edit().putString(SETTINGS_NUMBER_OF_PACKETS, value).apply();
        }
        screen.findPreference(SETTINGS_NUMBER_OF_PACKETS).setSummary(value);

		final int valueInt = Integer.parseInt(value);
		if (valueInt > 200 && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			new AlertDialog.Builder(requireContext()).setMessage(R.string.dfu_settings_dfu_number_of_packets_info).setTitle(R.string.dfu_settings_dfu_information)
					.setPositiveButton(R.string.ok, null)
					.show();
		}
	}

	private void updateMBRSize() {
		final PreferenceScreen screen = getPreferenceScreen();
		final SharedPreferences preferences = getPreferenceManager().getSharedPreferences();

		final String value = preferences.getString(SETTINGS_MBR_SIZE, String.valueOf(DfuServiceInitiator.DEFAULT_MBR_SIZE));
		screen.findPreference(SETTINGS_MBR_SIZE).setSummary(value);
	}
}
