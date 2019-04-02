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

package no.nordicsemi.android.nrftoolbox.csc.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceScreen;

import androidx.preference.PreferenceFragmentCompat;
import no.nordicsemi.android.nrftoolbox.R;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
	public static final String SETTINGS_WHEEL_SIZE = "settings_wheel_size";
	public static final int SETTINGS_WHEEL_SIZE_DEFAULT = 2340;
	public static final String SETTINGS_UNIT = "settings_csc_unit";
	public static final int SETTINGS_UNIT_M_S = 0; // [m/s]
	public static final int SETTINGS_UNIT_KM_H = 1; // [m/s]
	public static final int SETTINGS_UNIT_MPH = 2; // [m/s]
	public static final int SETTINGS_UNIT_DEFAULT = SETTINGS_UNIT_KM_H;

	@Override
	public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
		addPreferencesFromResource(R.xml.settings_csc);

		// set initial values
		updateWheelSizeSummary();
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
		if (SETTINGS_WHEEL_SIZE.equals(key)) {
			updateWheelSizeSummary();
		}
	}

	private void updateWheelSizeSummary() {
		final PreferenceScreen screen = getPreferenceScreen();
		final SharedPreferences preferences = getPreferenceManager().getSharedPreferences();

		final String value = preferences.getString(SETTINGS_WHEEL_SIZE, String.valueOf(SETTINGS_WHEEL_SIZE_DEFAULT));
		screen.findPreference(SETTINGS_WHEEL_SIZE).setSummary(getString(R.string.csc_settings_wheel_diameter_summary, value));
	}
}
