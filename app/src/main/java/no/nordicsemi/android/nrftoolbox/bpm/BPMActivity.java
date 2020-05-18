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
package no.nordicsemi.android.nrftoolbox.bpm;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.TextView;

import java.util.Calendar;
import java.util.UUID;

import no.nordicsemi.android.ble.common.profile.bp.BloodPressureMeasurementCallback;
import no.nordicsemi.android.ble.common.profile.bp.IntermediateCuffPressureCallback;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity;
import no.nordicsemi.android.nrftoolbox.profile.LoggableBleManager;

// TODO The BPMActivity should be rewritten to use the service approach, like other do.
public class BPMActivity extends BleProfileActivity implements BPMManagerCallbacks {
	@SuppressWarnings("unused")
	private static final String TAG = "BPMActivity";

	private TextView systolicView;
	private TextView systolicUnitView;
	private TextView diastolicView;
	private TextView diastolicUnitView;
	private TextView meanAPView;
	private TextView meanAPUnitView;
	private TextView pulseView;
	private TextView timestampView;
	private TextView batteryLevelView;

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_bpm);
		setGUI();
	}

	private void setGUI() {
		systolicView = findViewById(R.id.systolic);
		systolicUnitView = findViewById(R.id.systolic_unit);
		diastolicView = findViewById(R.id.diastolic);
		diastolicUnitView = findViewById(R.id.diastolic_unit);
		meanAPView = findViewById(R.id.mean_ap);
		meanAPUnitView = findViewById(R.id.mean_ap_unit);
		pulseView = findViewById(R.id.pulse);
		timestampView = findViewById(R.id.timestamp);
		batteryLevelView = findViewById(R.id.battery);
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.bpm_feature_title;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.bpm_default_name;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.bpm_about_text;
	}

	@Override
	protected UUID getFilterUUID() {
		return BPMManager.BP_SERVICE_UUID;
	}

	@Override
	protected LoggableBleManager<BPMManagerCallbacks> initializeManager() {
		final BPMManager manager = BPMManager.getBPMManager(getApplicationContext());
		manager.setGattCallbacks(this);
		return manager;
	}

	@Override
	protected void setDefaultUI() {
		systolicView.setText(R.string.not_available_value);
		systolicUnitView.setText(null);
		diastolicView.setText(R.string.not_available_value);
		diastolicUnitView.setText(null);
		meanAPView.setText(R.string.not_available_value);
		meanAPUnitView.setText(null);
		pulseView.setText(R.string.not_available_value);
		timestampView.setText(R.string.not_available);
		batteryLevelView.setText(R.string.not_available);
	}

	@Override
	public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
		// this may notify user or show some views
	}

	@Override
	public void onDeviceReady(@NonNull final BluetoothDevice device) {
		// this may notify user
	}

	@Override
	public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
		super.onDeviceDisconnected(device);
		runOnUiThread(() -> batteryLevelView.setText(R.string.not_available));
	}

	@Override
	public void onBloodPressureMeasurementReceived(@NonNull final BluetoothDevice device,
												   final float systolic, final float diastolic, final float meanArterialPressure, final int unit,
												   @Nullable final Float pulseRate, @Nullable final Integer userID,
												   @Nullable final BPMStatus status, @Nullable final Calendar calendar) {
		runOnUiThread(() -> {
			systolicView.setText(String.valueOf(systolic));
			diastolicView.setText(String.valueOf(diastolic));
			meanAPView.setText(String.valueOf(meanArterialPressure));
			if (pulseRate != null)
				pulseView.setText(String.valueOf(pulseRate));
			else
				pulseView.setText(R.string.not_available_value);
			if (calendar != null)
				timestampView.setText(getString(R.string.bpm_timestamp, calendar));
			else
				timestampView.setText(R.string.not_available);

			systolicUnitView.setText(unit == BloodPressureMeasurementCallback.UNIT_mmHg ? R.string.bpm_unit_mmhg : R.string.bpm_unit_kpa);
			diastolicUnitView.setText(unit == BloodPressureMeasurementCallback.UNIT_mmHg ? R.string.bpm_unit_mmhg : R.string.bpm_unit_kpa);
			meanAPUnitView.setText(unit == BloodPressureMeasurementCallback.UNIT_mmHg ? R.string.bpm_unit_mmhg : R.string.bpm_unit_kpa);
		});
	}

	@Override
	public void onIntermediateCuffPressureReceived(@NonNull final BluetoothDevice device, final float cuffPressure, final int unit,
												   @Nullable final Float pulseRate, @Nullable final Integer userID,
												   @Nullable final BPMStatus status, @Nullable final Calendar calendar) {
		runOnUiThread(() -> {
			systolicView.setText(String.valueOf(cuffPressure));
			diastolicView.setText(R.string.not_available_value);
			meanAPView.setText(R.string.not_available_value);
			if (pulseRate != null)
				pulseView.setText(String.valueOf(pulseRate));
			else
				pulseView.setText(R.string.not_available_value);
			if (calendar != null)
				timestampView.setText(getString(R.string.bpm_timestamp, calendar));
			else
				timestampView.setText(R.string.not_available);

			systolicUnitView.setText(unit == IntermediateCuffPressureCallback.UNIT_mmHg ? R.string.bpm_unit_mmhg : R.string.bpm_unit_kpa);
			diastolicUnitView.setText(null);
			meanAPUnitView.setText(null);
		});
	}

	@Override
	public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
		runOnUiThread(() -> batteryLevelView.setText(getString(R.string.battery, batteryLevel)));
	}
}
