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
package no.nordicsemi.android.nrftoolbox.gls;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.util.Calendar;
import java.util.UUID;

import no.nordicsemi.android.ble.common.callback.RecordAccessControlPointDataCallback;
import no.nordicsemi.android.ble.common.callback.glucose.GlucoseMeasurementContextDataCallback;
import no.nordicsemi.android.ble.common.callback.glucose.GlucoseMeasurementDataCallback;
import no.nordicsemi.android.ble.common.data.RecordAccessControlPointData;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.battery.BatteryManager;
import no.nordicsemi.android.nrftoolbox.parser.GlucoseMeasurementContextParser;
import no.nordicsemi.android.nrftoolbox.parser.GlucoseMeasurementParser;
import no.nordicsemi.android.nrftoolbox.parser.RecordAccessControlPointParser;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

@SuppressWarnings("unused")
public class GlucoseManager extends BatteryManager<GlucoseManagerCallbacks> {
	private static final String TAG = "GlucoseManager";

	/** Glucose service UUID */
	final static UUID GLS_SERVICE_UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");
	/** Glucose Measurement characteristic UUID */
	private final static UUID GM_CHARACTERISTIC = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb");
	/** Glucose Measurement Context characteristic UUID */
	private final static UUID GM_CONTEXT_CHARACTERISTIC = UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb");
	/** Glucose Feature characteristic UUID */
	private final static UUID GF_CHARACTERISTIC = UUID.fromString("00002A51-0000-1000-8000-00805f9b34fb");
	/** Record Access Control Point characteristic UUID */
	private final static UUID RACP_CHARACTERISTIC = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic glucoseMeasurementCharacteristic;
	private BluetoothGattCharacteristic glucoseMeasurementContextCharacteristic;
	private BluetoothGattCharacteristic recordAccessControlPointCharacteristic;

	private final SparseArray<GlucoseRecord> records = new SparseArray<>();
	private Handler handler;
	private static GlucoseManager instance;

	/**
	 * Returns the singleton implementation of GlucoseManager.
	 */
	static GlucoseManager getGlucoseManager(@NonNull final Context context) {
		if (instance == null)
			instance = new GlucoseManager(context);
		return instance;
	}

	private GlucoseManager(final Context context) {
		super(context);
		handler = new Handler();
	}

	@NonNull
	@Override
	protected BatteryManagerGattCallback getGattCallback() {
		return new GlucoseManagerGattCallback();
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery,
	 * receiving notification, etc.
	 */
	private class GlucoseManagerGattCallback extends BatteryManagerGattCallback {

		@Override
		protected void initialize() {
			super.initialize();

			// The gatt.setCharacteristicNotification(...) method is called in BleManager during
			// enabling notifications or indications
			// (see BleManager#internalEnableNotifications/Indications).
			// However, on Samsung S3 with Android 4.3 it looks like the 2 gatt calls
			// (gatt.setCharacteristicNotification(...) and gatt.writeDescriptor(...)) are called
			// too quickly, or from a wrong thread, and in result the notification listener is not
			// set, causing onCharacteristicChanged(...) callback never being called when a
			// notification comes. Enabling them here, like below, solves the problem.
			// However... the original approach works for the Battery Level CCCD, which makes it
			// even weirder.
			/*
			gatt.setCharacteristicNotification(glucoseMeasurementCharacteristic, true);
			if (glucoseMeasurementContextCharacteristic != null) {
				device.setCharacteristicNotification(glucoseMeasurementContextCharacteristic, true);
			}
			device.setCharacteristicNotification(recordAccessControlPointCharacteristic, true);
			*/
			setNotificationCallback(glucoseMeasurementCharacteristic)
					.with(new GlucoseMeasurementDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + GlucoseMeasurementParser.parse(data) + "\" received");
							super.onDataReceived(device, data);
						}

						@Override
						public void onGlucoseMeasurementReceived(@NonNull final BluetoothDevice device, final int sequenceNumber,
																 @NonNull final Calendar time, @Nullable final Float glucoseConcentration,
																 @Nullable final Integer unit, @Nullable final Integer type,
																 @Nullable final Integer sampleLocation, @Nullable final GlucoseStatus status,
																 final boolean contextInformationFollows) {
							final GlucoseRecord record = new GlucoseRecord();
							record.sequenceNumber = sequenceNumber;
							record.time = time;
							record.glucoseConcentration = glucoseConcentration != null ? glucoseConcentration : 0;
							record.unit = unit != null ? unit : UNIT_kg_L;
							record.type = type != null ? type : 0;
							record.sampleLocation = sampleLocation != null ? sampleLocation : 0;
							record.status = status != null ? status.value : 0;

							// insert the new record to storage
							records.put(record.sequenceNumber, record);
							handler.post(() -> {
								// if there is no context information following the measurement data,
								// notify callback about the new record
								if (!contextInformationFollows)
									mCallbacks.onDataSetChanged(device);
							});
						}
					});

			setNotificationCallback(glucoseMeasurementContextCharacteristic)
					.with(new GlucoseMeasurementContextDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + GlucoseMeasurementContextParser.parse(data) + "\" received");
							super.onDataReceived(device, data);
						}

						@Override
						public void onGlucoseMeasurementContextReceived(@NonNull final BluetoothDevice device, final int sequenceNumber,
																		@Nullable final Carbohydrate carbohydrate, @Nullable final Float carbohydrateAmount,
																		@Nullable final Meal meal, @Nullable final Tester tester,
																		@Nullable final Health health, @Nullable final Integer exerciseDuration,
																		@Nullable final Integer exerciseIntensity, @Nullable final Medication medication,
																		@Nullable final Float medicationAmount, @Nullable final Integer medicationUnit,
																		@Nullable final Float HbA1c) {
							final GlucoseRecord record = records.get(sequenceNumber);
							if (record == null) {
								DebugLogger.w(TAG, "Context information with unknown sequence number: " + sequenceNumber);
								return;
							}
							final GlucoseRecord.MeasurementContext context = new GlucoseRecord.MeasurementContext();
							record.context = context;
							context.carbohydrateId = carbohydrate != null ? carbohydrate.value : 0;
							context.carbohydrateUnits = carbohydrateAmount != null ? carbohydrateAmount : 0;
							context.meal = meal != null ? meal.value : 0;
							context.tester = tester != null ? tester.value : 0;
							context.health = health != null ? health.value : 0;
							context.exerciseDuration = exerciseDuration != null ? exerciseDuration : 0;
							context.exerciseIntensity = exerciseIntensity != null ? exerciseIntensity : 0;
							context.medicationId = medication != null ? medication.value : 0;
							context.medicationQuantity = medicationAmount != null ? medicationAmount : 0;
							context.medicationUnit = medicationUnit != null ? medicationUnit : UNIT_mg;
							context.HbA1c = HbA1c != null ? HbA1c : 0;

							handler.post(() -> {
								// notify callback about the new record
								mCallbacks.onDataSetChanged(device);
							});
						}
					});

			setIndicationCallback(recordAccessControlPointCharacteristic)
					.with(new RecordAccessControlPointDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" received");
							super.onDataReceived(device, data);
						}

						@SuppressLint("SwitchIntDef")
						@Override
						public void onRecordAccessOperationCompleted(@NonNull final BluetoothDevice device,
																	 @RACPOpCode final int requestCode) {
							//noinspection SwitchStatementWithTooFewBranches
							switch (requestCode) {
								case RACP_OP_CODE_ABORT_OPERATION:
									mCallbacks.onOperationAborted(device);
									break;
								default:
									mCallbacks.onOperationCompleted(device);
									break;
							}
						}

						@Override
						public void onRecordAccessOperationCompletedWithNoRecordsFound(@NonNull final BluetoothDevice device,
																					   @RACPOpCode final int requestCode) {
							mCallbacks.onOperationCompleted(device);
						}

						@Override
						public void onNumberOfRecordsReceived(@NonNull final BluetoothDevice device, final int numberOfRecords) {
							mCallbacks.onNumberOfRecordsRequested(device, numberOfRecords);
							if (numberOfRecords > 0) {
								if (records.size() > 0) {
									final int sequenceNumber = records.keyAt(records.size() - 1) + 1;
									writeCharacteristic(recordAccessControlPointCharacteristic,
											RecordAccessControlPointData.reportStoredRecordsGreaterThenOrEqualTo(sequenceNumber))
											.enqueue();
								} else {
									writeCharacteristic(recordAccessControlPointCharacteristic,
											RecordAccessControlPointData.reportAllStoredRecords())
											.enqueue();
								}
							} else {
								mCallbacks.onOperationCompleted(device);
							}
						}

						@Override
						public void onRecordAccessOperationError(@NonNull final BluetoothDevice device,
																 @RACPOpCode final int requestCode,
																 @RACPErrorCode final int errorCode) {
							log(Log.WARN, "Record Access operation failed (error " + errorCode + ")");
							if (errorCode == RACP_ERROR_OP_CODE_NOT_SUPPORTED) {
								mCallbacks.onOperationNotSupported(device);
							} else {
								mCallbacks.onOperationFailed(device);
							}
						}
					});

			enableNotifications(glucoseMeasurementCharacteristic).enqueue();
			enableNotifications(glucoseMeasurementContextCharacteristic).enqueue();
			enableIndications(recordAccessControlPointCharacteristic)
					.fail((device, status) -> log(Log.WARN, "Failed to enabled Record Access Control Point indications (error " + status + ")"))
					.enqueue();
		}

		@Override
		public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(GLS_SERVICE_UUID);
			if (service != null) {
				glucoseMeasurementCharacteristic = service.getCharacteristic(GM_CHARACTERISTIC);
				glucoseMeasurementContextCharacteristic = service.getCharacteristic(GM_CONTEXT_CHARACTERISTIC);
				recordAccessControlPointCharacteristic = service.getCharacteristic(RACP_CHARACTERISTIC);
			}
			return glucoseMeasurementCharacteristic != null && recordAccessControlPointCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(@NonNull BluetoothGatt gatt) {
			super.isOptionalServiceSupported(gatt);
			return glucoseMeasurementContextCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			glucoseMeasurementCharacteristic = null;
			glucoseMeasurementContextCharacteristic = null;
			recordAccessControlPointCharacteristic = null;
		}
	}

	/**
	 * Returns all records as a sparse array where sequence number is the key.
	 *
	 * @return the records list.
	 */
	SparseArray<GlucoseRecord> getRecords() {
		return records;
	}

	/**
	 * Clears the records list locally.
	 */
	public void clear() {
		records.clear();
		final BluetoothDevice target = getBluetoothDevice();
		if (target != null) {
			mCallbacks.onOperationCompleted(target);
		}
	}

	/**
	 * Sends the request to obtain the last (most recent) record from glucose device. The data will
	 * be returned to Glucose Measurement characteristic as a notification followed by Record Access
	 * Control Point indication with status code Success or other in case of error.
	 */
	void getLastRecord() {
		if (recordAccessControlPointCharacteristic == null)
			return;
		final BluetoothDevice target = getBluetoothDevice();
		if (target == null)
			return;

		clear();
		mCallbacks.onOperationStarted(target);
		writeCharacteristic(recordAccessControlPointCharacteristic, RecordAccessControlPointData.reportLastStoredRecord())
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
				.enqueue();
	}

	/**
	 * Sends the request to obtain the first (oldest) record from glucose device. The data will be
	 * returned to Glucose Measurement characteristic as a notification followed by Record Access
	 * Control Point indication with status code Success or other in case of error.
	 */
	void getFirstRecord() {
		if (recordAccessControlPointCharacteristic == null)
			return;
		final BluetoothDevice target = getBluetoothDevice();
		if (target == null)
			return;

		clear();
		mCallbacks.onOperationStarted(target);
		writeCharacteristic(recordAccessControlPointCharacteristic, RecordAccessControlPointData.reportFirstStoredRecord())
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
				.enqueue();
	}

	/**
	 * Sends the request to obtain all records from glucose device. Initially we want to notify user
	 * about the number of the records so the 'Report Number of Stored Records' is send. The data
	 * will be returned to Glucose Measurement characteristic as a notification followed by
	 * Record Access Control Point indication with status code Success or other in case of error.
	 */
	void getAllRecords() {
		if (recordAccessControlPointCharacteristic == null)
			return;
		final BluetoothDevice target = getBluetoothDevice();
		if (target == null)
			return;

		clear();
		mCallbacks.onOperationStarted(target);
		writeCharacteristic(recordAccessControlPointCharacteristic, RecordAccessControlPointData.reportNumberOfAllStoredRecords())
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
				.enqueue();
	}

	/**
	 * Sends the request to obtain from the glucose device all records newer than the newest one
	 * from local storage. The data will be returned to Glucose Measurement characteristic as
	 * a notification followed by Record Access Control Point indication with status code Success
	 * or other in case of error.
	 * <p>
	 * Refresh button will not download records older than the oldest in the local memory.
	 * E.g. if you have pressed Last and then Refresh, than it will try to get only newer records.
	 * However if there are no records, it will download all existing (using {@link #getAllRecords()}).
	 */
	void refreshRecords() {
		if (recordAccessControlPointCharacteristic == null)
			return;
		final BluetoothDevice target = getBluetoothDevice();
		if (target == null)
			return;

		if (records.size() == 0) {
			getAllRecords();
		} else {
			mCallbacks.onOperationStarted(target);

			// obtain the last sequence number
			final int sequenceNumber = records.keyAt(records.size() - 1) + 1;

			writeCharacteristic(recordAccessControlPointCharacteristic,
					RecordAccessControlPointData.reportStoredRecordsGreaterThenOrEqualTo(sequenceNumber))
					.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
					.enqueue();
			// Info:
			// Operators OPERATOR_LESS_THEN_OR_EQUAL and OPERATOR_RANGE are not supported by Nordic Semiconductor Glucose Service in SDK 4.4.2.
		}
	}

	/**
	 * Sends abort operation signal to the device.
	 */
	void abort() {
		if (recordAccessControlPointCharacteristic == null)
			return;
		final BluetoothDevice target = getBluetoothDevice();
		if (target == null)
			return;

		writeCharacteristic(recordAccessControlPointCharacteristic, RecordAccessControlPointData.abortOperation())
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
				.enqueue();
	}

	/**
	 * Sends the request to delete all data from the device. A Record Access Control Point
	 * indication with status code Success (or other in case of error) will be send.
	 */
	void deleteAllRecords() {
		if (recordAccessControlPointCharacteristic == null)
			return;
		final BluetoothDevice target = getBluetoothDevice();
		if (target == null)
			return;

		clear();
		mCallbacks.onOperationStarted(target);
		writeCharacteristic(recordAccessControlPointCharacteristic, RecordAccessControlPointData.deleteAllStoredRecords())
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
				.enqueue();
	}
}
