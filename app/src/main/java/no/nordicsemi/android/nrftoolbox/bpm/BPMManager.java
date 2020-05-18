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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;
import java.util.UUID;

import no.nordicsemi.android.ble.common.callback.bps.BloodPressureMeasurementDataCallback;
import no.nordicsemi.android.ble.common.callback.bps.IntermediateCuffPressureDataCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.battery.BatteryManager;
import no.nordicsemi.android.nrftoolbox.parser.BloodPressureMeasurementParser;
import no.nordicsemi.android.nrftoolbox.parser.IntermediateCuffPressureParser;

@SuppressWarnings({"unused", "WeakerAccess"})
public class BPMManager extends BatteryManager<BPMManagerCallbacks> {
	/** Blood Pressure service UUID. */
	public final static UUID BP_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
	/** Blood Pressure Measurement characteristic UUID. */
	private static final UUID BPM_CHARACTERISTIC_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb");
	/** Intermediate Cuff Pressure characteristic UUID. */
	private static final UUID ICP_CHARACTERISTIC_UUID = UUID.fromString("00002A36-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic bpmCharacteristic, icpCharacteristic;

	private static BPMManager managerInstance = null;

	/**
	 * Returns the singleton implementation of BPMManager.
	 */
	public static synchronized BPMManager getBPMManager(final Context context) {
		if (managerInstance == null) {
			managerInstance = new BPMManager(context);
		}
		return managerInstance;
	}

	private BPMManager(final Context context) {
		super(context);
	}

	@NonNull
	@Override
	protected BatteryManagerGattCallback getGattCallback() {
		return new BloodPressureManagerGattCallback();
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery,
	 * receiving notification, etc.
	 */
	private class BloodPressureManagerGattCallback extends BatteryManagerGattCallback {

		@Override
		protected void initialize() {
			super.initialize();

			setNotificationCallback(icpCharacteristic)
					.with(new IntermediateCuffPressureDataCallback() {
							  @Override
							  public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
								  log(LogContract.Log.Level.APPLICATION, "\"" + IntermediateCuffPressureParser.parse(data) + "\" received");

								  // Pass through received data
								  super.onDataReceived(device, data);
							  }

							  @Override
							  public void onIntermediateCuffPressureReceived(@NonNull final BluetoothDevice device,
																			 final float cuffPressure, final int unit,
																			 @Nullable final Float pulseRate, @Nullable final Integer userID,
																			 @Nullable final BPMStatus status, @Nullable final Calendar calendar) {
								  mCallbacks.onIntermediateCuffPressureReceived(device, cuffPressure, unit, pulseRate, userID, status, calendar);
							  }

							  @Override
							  public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
								  log(Log.WARN, "Invalid ICP data received: " + data);
							  }
						  });
			setIndicationCallback(bpmCharacteristic)
					.with(new BloodPressureMeasurementDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + BloodPressureMeasurementParser.parse(data) + "\" received");

							// Pass through received data
							super.onDataReceived(device, data);
						}

						@Override
						public void onBloodPressureMeasurementReceived(@NonNull final BluetoothDevice device,
																	   final float systolic, final float diastolic, final float meanArterialPressure,
																	   final int unit, @Nullable final Float pulseRate,
																	   @Nullable final Integer userID, @Nullable final BPMStatus status,
																	   @Nullable final Calendar calendar) {
							mCallbacks.onBloodPressureMeasurementReceived(device, systolic, diastolic,
                                    meanArterialPressure, unit, pulseRate, userID, status, calendar);
						}

						@Override
						public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(Log.WARN, "Invalid BPM data received: " + data);
						}
					});

			enableNotifications(icpCharacteristic)
					.fail((device, status) -> log(Log.WARN,
							"Intermediate Cuff Pressure characteristic not found"))
					.enqueue();
			enableIndications(bpmCharacteristic).enqueue();
		}

		@Override
		protected boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(BP_SERVICE_UUID);
			if (service != null) {
				bpmCharacteristic = service.getCharacteristic(BPM_CHARACTERISTIC_UUID);
				icpCharacteristic = service.getCharacteristic(ICP_CHARACTERISTIC_UUID);
			}
			return bpmCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
			super.isOptionalServiceSupported(gatt); // ignore the result of this
			return icpCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			icpCharacteristic = null;
			bpmCharacteristic = null;
		}
	}
}
