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
package no.nordicsemi.android.nrftoolbox.hr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.TextView;

import org.achartengine.GraphicalView;

import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.FeaturesActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity;
import no.nordicsemi.android.nrftoolbox.profile.LoggableBleManager;

/**
 * HRSActivity is the main Heart rate activity. It implements HRSManagerCallbacks to receive callbacks from HRSManager class. The activity supports portrait and landscape orientations. The activity
 * uses external library AChartEngine to show real time graph of HR values.
 */
// TODO The HRSActivity should be rewritten to use the service approach, like other do.
public class HRActivity extends BleProfileActivity implements HRManagerCallbacks {
	@SuppressWarnings("unused")
	private final String TAG = "HRSActivity";

	private final static String GRAPH_STATUS = "graph_status";
	private final static String GRAPH_COUNTER = "graph_counter";
	private final static String HR_VALUE = "hr_value";

	private final static int REFRESH_INTERVAL = 1000; // 1 second interval

	private Handler handler = new Handler();

	private boolean isGraphInProgress = false;

	private GraphicalView graphView;
	private LineGraphView lineGraph;
	private TextView hrValueView, hrLocationView;
	private TextView batteryLevelView;

	private int hrValue = 0;
	private int counter = 0;

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.activity_feature_hrs);
		setGUI();
	}

	private void setGUI() {
		lineGraph = LineGraphView.getLineGraphView();
		hrValueView = findViewById(R.id.text_hrs_value);
		hrLocationView = findViewById(R.id.text_hrs_position);
		batteryLevelView = findViewById(R.id.battery);
		showGraph();
	}

	private void showGraph() {
		graphView = lineGraph.getView(this);
		ViewGroup layout = findViewById(R.id.graph_hrs);
		layout.addView(graphView);
	}

	@Override
	protected void onStart() {
		super.onStart();

		final Intent intent = getIntent();
		if (!isDeviceConnected() && intent.hasExtra(FeaturesActivity.EXTRA_ADDRESS)) {
			final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(getIntent().getByteArrayExtra(FeaturesActivity.EXTRA_ADDRESS));
			onDeviceSelected(device, device.getName());

			intent.removeExtra(FeaturesActivity.EXTRA_APP);
			intent.removeExtra(FeaturesActivity.EXTRA_ADDRESS);
		}
	}

	@Override
	protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		isGraphInProgress = savedInstanceState.getBoolean(GRAPH_STATUS);
		counter = savedInstanceState.getInt(GRAPH_COUNTER);
		hrValue = savedInstanceState.getInt(HR_VALUE);

		if (isGraphInProgress)
			startShowGraph();
	}

	@Override
	protected void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(GRAPH_STATUS, isGraphInProgress);
		outState.putInt(GRAPH_COUNTER, counter);
		outState.putInt(HR_VALUE, hrValue);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		stopShowGraph();
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.hrs_feature_title;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.hrs_about_text;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.hrs_default_name;
	}

	@Override
	protected UUID getFilterUUID() {
		return HRManager.HR_SERVICE_UUID;
	}

	private void updateGraph(final int hrmValue) {
		counter++;
		lineGraph.addValue(new Point(counter, hrmValue));
		graphView.repaint();
	}

	private Runnable repeatTask = new Runnable() {
		@Override
		public void run() {
			if (hrValue > 0)
				updateGraph(hrValue);
			if (isGraphInProgress)
				handler.postDelayed(repeatTask, REFRESH_INTERVAL);
		}
	};

	void startShowGraph() {
		isGraphInProgress = true;
		repeatTask.run();
	}

	void stopShowGraph() {
		isGraphInProgress = false;
		handler.removeCallbacks(repeatTask);
	}

	@Override
	protected LoggableBleManager<HRManagerCallbacks> initializeManager() {
		final HRManager manager = HRManager.getInstance(getApplicationContext());
		manager.setGattCallbacks(this);
		return manager;
	}

	@Override
	public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
		// this may notify user or show some views
	}

	@Override
	public void onDeviceReady(@NonNull final BluetoothDevice device) {
		startShowGraph();
	}

	@Override
	public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
		runOnUiThread(() -> this.batteryLevelView.setText(getString(R.string.battery, batteryLevel)));
	}

	@Override
	public void onBodySensorLocationReceived(@NonNull final BluetoothDevice device, final int sensorLocation) {
		runOnUiThread(() -> {
			if (sensorLocation >= SENSOR_LOCATION_FIRST && sensorLocation <= SENSOR_LOCATION_LAST) {
				hrLocationView.setText(getResources().getStringArray(R.array.hrs_locations)[sensorLocation]);
			} else {
				hrLocationView.setText(R.string.hrs_location_other);
			}
		});
	}

	@Override
	public void onHeartRateMeasurementReceived(@NonNull final BluetoothDevice device,
											   @IntRange(from = 0) final int heartRate,
											   @Nullable final Boolean contactDetected,
											   @Nullable @IntRange(from = 0) final Integer energyExpanded,
											   @Nullable final List<Integer> rrIntervals) {
		hrValue = heartRate;
		runOnUiThread(() -> hrValueView.setText(getString(R.string.hrs_value, heartRate)));
	}

	@Override
	public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
		super.onDeviceDisconnected(device);
		runOnUiThread(() -> {
			hrValueView.setText(R.string.not_available_value);
			hrLocationView.setText(R.string.not_available);
			batteryLevelView.setText(R.string.not_available);
			stopShowGraph();
		});
	}

	@Override
	protected void setDefaultUI() {
		hrValueView.setText(R.string.not_available_value);
		hrLocationView.setText(R.string.not_available);
		batteryLevelView.setText(R.string.not_available);
		clearGraph();
	}

	private void clearGraph() {
		lineGraph.clearGraph();
		graphView.repaint();
		counter = 0;
		hrValue = 0;
	}
}
