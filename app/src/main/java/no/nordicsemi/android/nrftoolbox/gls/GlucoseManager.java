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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.util.SparseArray;

import java.util.Calendar;
import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.Request;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.parser.GlucoseMeasurementContextParser;
import no.nordicsemi.android.nrftoolbox.parser.GlucoseMeasurementParser;
import no.nordicsemi.android.nrftoolbox.parser.RecordAccessControlPointParser;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

@SuppressWarnings("unused")
public class GlucoseManager extends BleManager<GlucoseManagerCallbacks> {
	private static final String TAG = "GlucoseManager";

	/**
	 * Glucose service UUID
	 */
	public final static UUID GLS_SERVICE_UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");
	/**
	 * Glucose Measurement characteristic UUID
	 */
	private final static UUID GM_CHARACTERISTIC = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb");
	/**
	 * Glucose Measurement Context characteristic UUID
	 */
	private final static UUID GM_CONTEXT_CHARACTERISTIC = UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb");
	/**
	 * Glucose Feature characteristic UUID
	 */
	private final static UUID GF_CHARACTERISTIC = UUID.fromString("00002A51-0000-1000-8000-00805f9b34fb");
	/**
	 * Record Access Control Point characteristic UUID
	 */
	private final static UUID RACP_CHARACTERISTIC = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb");

	private final static int OP_CODE_REPORT_STORED_RECORDS = 1;
	private final static int OP_CODE_DELETE_STORED_RECORDS = 2;
	private final static int OP_CODE_ABORT_OPERATION = 3;
	private final static int OP_CODE_REPORT_NUMBER_OF_RECORDS = 4;
	private final static int OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5;
	private final static int OP_CODE_RESPONSE_CODE = 6;

	private final static int OPERATOR_NULL = 0;
	private final static int OPERATOR_ALL_RECORDS = 1;
	private final static int OPERATOR_LESS_THEN_OR_EQUAL = 2;
	private final static int OPERATOR_GREATER_THEN_OR_EQUAL = 3;
	private final static int OPERATOR_WITHING_RANGE = 4;
	private final static int OPERATOR_FIRST_RECORD = 5;
	private final static int OPERATOR_LAST_RECORD = 6;

	/**
	 * The filter type is used for range operators ({@link #OPERATOR_LESS_THEN_OR_EQUAL},
     * {@link #OPERATOR_GREATER_THEN_OR_EQUAL}, {@link #OPERATOR_WITHING_RANGE}.<br/>
	 * The syntax of the operand is: [Filter Type][Minimum][Maximum].<br/>
	 * This filter selects the records by the sequence number.
	 */
	private final static int FILTER_TYPE_SEQUENCE_NUMBER = 1;
	/**
	 * The filter type is used for range operators ({@link #OPERATOR_LESS_THEN_OR_EQUAL},
     * {@link #OPERATOR_GREATER_THEN_OR_EQUAL}, {@link #OPERATOR_WITHING_RANGE}.<br/>
	 * The syntax of the operand is: [Filter Type][Minimum][Maximum].<br/>
	 * This filter selects the records by the user facing time (base time + offset time).
	 */
	private final static int FILTER_TYPE_USER_FACING_TIME = 2;

	private final static int RESPONSE_SUCCESS = 1;
	private final static int RESPONSE_OP_CODE_NOT_SUPPORTED = 2;
	private final static int RESPONSE_INVALID_OPERATOR = 3;
	private final static int RESPONSE_OPERATOR_NOT_SUPPORTED = 4;
	private final static int RESPONSE_INVALID_OPERAND = 5;
	private final static int RESPONSE_NO_RECORDS_FOUND = 6;
	private final static int RESPONSE_ABORT_UNSUCCESSFUL = 7;
	private final static int RESPONSE_PROCEDURE_NOT_COMPLETED = 8;
	private final static int RESPONSE_OPERAND_NOT_SUPPORTED = 9;

	private BluetoothGattCharacteristic mGlucoseMeasurementCharacteristic;
	private BluetoothGattCharacteristic mGlucoseMeasurementContextCharacteristic;
	private BluetoothGattCharacteristic mRecordAccessControlPointCharacteristic;

	private final SparseArray<GlucoseRecord> mRecords = new SparseArray<>();
	private boolean mAbort;
	private Handler mHandler;
	private static GlucoseManager mInstance;

	/**
	 * Returns the singleton implementation of GlucoseManager
	 */
	public static GlucoseManager getGlucoseManager(final Context context) {
		if (mInstance == null)
			mInstance = new GlucoseManager(context);
		return mInstance;
	}

	public GlucoseManager(final Context context) {
		super(context);
		mHandler = new Handler();
	}

	@Override
	protected BleManagerGattCallback getGattCallback() {
		return mGattCallback;
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving notification, etc
	 */
	private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

		@Override
		protected Deque<Request> initGatt(final BluetoothGatt gatt) {
			final LinkedList<Request> requests = new LinkedList<>();
			requests.add(Request.newEnableNotificationsRequest(mGlucoseMeasurementCharacteristic));
			if (mGlucoseMeasurementContextCharacteristic != null) {
				requests.add(Request.newEnableNotificationsRequest(mGlucoseMeasurementContextCharacteristic));
			}
			requests.add(Request.newEnableIndicationsRequest(mRecordAccessControlPointCharacteristic));

			// The gatt.setCharacteristicNotification(...) method is called in BleManager during enabling
			// notifications or indications (see BleManager#internalEnableNotifications/Indications).
			// However, on Samsung S3 with Android 4.3 it looks like the 2 gatt calls
			// (gatt.setCharacteristicNotification(...) and gatt.writeDescriptor(...)) are called
			// too quickly, or from a wrong thread, and in result the notification listener is not set,
			// causing onCharacteristicChanged(...) callback never being called when a notification comes.
			// Enabling them here, like below, solves the problem.
			// However... the original approach works for the Battery Level CCCD, which makes it even weirder.
			gatt.setCharacteristicNotification(mGlucoseMeasurementCharacteristic, true);
			if (mGlucoseMeasurementContextCharacteristic != null) {
				gatt.setCharacteristicNotification(mGlucoseMeasurementContextCharacteristic, true);
			}
			gatt.setCharacteristicNotification(mRecordAccessControlPointCharacteristic, true);
			return requests;
		}

		@Override
		public boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(GLS_SERVICE_UUID);
			if (service != null) {
				mGlucoseMeasurementCharacteristic = service.getCharacteristic(GM_CHARACTERISTIC);
				mGlucoseMeasurementContextCharacteristic = service.getCharacteristic(GM_CONTEXT_CHARACTERISTIC);
				mRecordAccessControlPointCharacteristic = service.getCharacteristic(RACP_CHARACTERISTIC);
			}
			return mGlucoseMeasurementCharacteristic != null && mRecordAccessControlPointCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(BluetoothGatt gatt) {
			return mGlucoseMeasurementContextCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			mGlucoseMeasurementCharacteristic = null;
			mGlucoseMeasurementContextCharacteristic = null;
			mRecordAccessControlPointCharacteristic = null;
		}

		@Override
		protected void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			Logger.a(mLogSession, "\"" + RecordAccessControlPointParser.parse(characteristic) + "\" sent");
		}

		@Override
		public void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			final UUID uuid = characteristic.getUuid();

			if (GM_CHARACTERISTIC.equals(uuid)) {
				Logger.a(mLogSession, "\"" + GlucoseMeasurementParser.parse(characteristic) + "\" received");

				int offset = 0;
				final int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
				offset += 1;

				final boolean timeOffsetPresent = (flags & 0x01) > 0;
				final boolean typeAndLocationPresent = (flags & 0x02) > 0;
				final int concentrationUnit = (flags & 0x04) > 0 ? GlucoseRecord.UNIT_molpl : GlucoseRecord.UNIT_kgpl;
				final boolean sensorStatusAnnunciationPresent = (flags & 0x08) > 0;
				final boolean contextInfoFollows = (flags & 0x10) > 0;

				// create and fill the new record
				final GlucoseRecord record = new GlucoseRecord();
				record.sequenceNumber = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
				offset += 2;

				final int year = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
				final int month = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2) - 1; // months are 1-based
				final int day = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 3);
				final int hours = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 4);
				final int minutes = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 5);
				final int seconds = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 6);
				offset += 7;

				final Calendar calendar = Calendar.getInstance();
				calendar.set(year, month, day, hours, minutes, seconds);
				record.time = calendar;

				if (timeOffsetPresent) {
					record.timeOffset = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
					calendar.add(Calendar.MINUTE, record.timeOffset);
					offset += 2;
				}

				if (typeAndLocationPresent) {
					record.glucoseConcentration = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
					record.unit = concentrationUnit;
					final int typeAndLocation = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2);
					record.type = (typeAndLocation & 0x0F);
					record.sampleLocation = (typeAndLocation & 0xF0) >> 4;
					offset += 3;
				}

				if (sensorStatusAnnunciationPresent) {
					record.status = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
				}
				// This allows you to check other values that are not provided by the Nordic Semiconductor's Glucose Service in SDK 4.4.2.
				//				record.status = 0x1A;
				//				record.context = new GlucoseRecord.MeasurementContext();
				//				record.context.carbohydrateId = 1;
				//				record.context.carbohydrateUnits = 0.23f;
				//				record.context.meal = 2;
				//				record.context.tester = 2;
				//				record.context.health = 4;
				// the following values are not implemented yet (see ExpandableRecordAdapter#getChildrenCount() and #getChild(...)
				//				record.context.exerciseDuration = 3600;
				//				record.context.exerciseIntensity = 45;
				//				record.context.medicationId = 3;
				//				record.context.medicationQuantity = 0.03f;
				//				record.context.medicationUnit = GlucoseRecord.MeasurementContext.UNIT_kg;
				//				record.context.HbA1c = 213.3f;

				// data set modifications must be done in UI thread
				mHandler.post(() -> {
					// insert the new record to storage
					mRecords.put(record.sequenceNumber, record);

					// if there is no context information following the measurement data, notify callback about the new record
					if (!contextInfoFollows)
						mCallbacks.onDatasetChanged(gatt.getDevice());
				});
			} else if (GM_CONTEXT_CHARACTERISTIC.equals(uuid)) {
				Logger.a(mLogSession, "\"" + GlucoseMeasurementContextParser.parse(characteristic) + "\" received");

				int offset = 0;
				final int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
				offset += 1;

				final boolean carbohydratePresent = (flags & 0x01) > 0;
				final boolean mealPresent = (flags & 0x02) > 0;
				final boolean testerHealthPresent = (flags & 0x04) > 0;
				final boolean exercisePresent = (flags & 0x08) > 0;
				final boolean medicationPresent = (flags & 0x10) > 0;
				final int medicationUnit = (flags & 0x20) > 0 ? GlucoseRecord.MeasurementContext.UNIT_l : GlucoseRecord.MeasurementContext.UNIT_kg;
				final boolean hbA1cPresent = (flags & 0x40) > 0;
				final boolean moreFlagsPresent = (flags & 0x80) > 0;

				final int sequenceNumber = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
				offset += 2;

				final GlucoseRecord record = mRecords.get(sequenceNumber);
				if (record == null) {
					DebugLogger.w(TAG, "Context information with unknown sequence number: " + sequenceNumber);
					return;
				}

				final GlucoseRecord.MeasurementContext context = new GlucoseRecord.MeasurementContext();
				record.context = context;

				if (moreFlagsPresent)
					offset += 1;

				if (carbohydratePresent) {
					context.carbohydrateId = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
					context.carbohydrateUnits = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset + 1);
					offset += 3;
				}

				if (mealPresent) {
					context.meal = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
					offset += 1;
				}

				if (testerHealthPresent) {
					final int testerHealth = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
					context.tester = (testerHealth & 0xF0) >> 4;
					context.health = (testerHealth & 0x0F);
					offset += 1;
				}

				if (exercisePresent) {
					context.exerciseDuration = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
					context.exerciseIntensity = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2);
					offset += 3;
				}

				if (medicationPresent) {
					context.medicationId = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
					context.medicationQuantity = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset + 1);
					context.medicationUnit = medicationUnit;
					offset += 3;
				}

				if (hbA1cPresent) {
					context.HbA1c = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
				}

				// notify callback about the new record
				mCallbacks.onDatasetChanged(gatt.getDevice());
			}
		}

		@Override
		protected void onCharacteristicIndicated(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			Logger.a(mLogSession, "\"" + RecordAccessControlPointParser.parse(characteristic) + "\" received");

			// Record Access Control Point characteristic
			int offset = 0;
			final int opCode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
			offset += 2; // skip the operator

			if (opCode == OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE) {
				// We've obtained the number of all records
				final int number = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);

				mCallbacks.onNumberOfRecordsRequested(gatt.getDevice(), number);

				// Request the records
				if (number > 0) {
					final BluetoothGattCharacteristic racpCharacteristic = mRecordAccessControlPointCharacteristic;
					writeCharacteristic(racpCharacteristic, getOpCode(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_ALL_RECORDS));
				} else {
					mCallbacks.onOperationCompleted(gatt.getDevice());
				}
			} else if (opCode == OP_CODE_RESPONSE_CODE) {
				final int requestedOpCode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
				final int responseCode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1);
				DebugLogger.d(TAG, "Response result for: " + requestedOpCode + " is: " + responseCode);

				switch (responseCode) {
					case RESPONSE_SUCCESS:
						if (!mAbort)
							mCallbacks.onOperationCompleted(gatt.getDevice());
						else
							mCallbacks.onOperationAborted(gatt.getDevice());
						break;
					case RESPONSE_NO_RECORDS_FOUND:
						mCallbacks.onOperationCompleted(gatt.getDevice());
						break;
					case RESPONSE_OP_CODE_NOT_SUPPORTED:
						mCallbacks.onOperationNotSupported(gatt.getDevice());
						break;
					case RESPONSE_PROCEDURE_NOT_COMPLETED:
					case RESPONSE_ABORT_UNSUCCESSFUL:
					default:
						mCallbacks.onOperationFailed(gatt.getDevice());
						break;
				}
				mAbort = false;
			}
		}
	};

	/**
	 * Writes given operation parameters to the characteristic
	 *
	 * @param opCode   the operation code
	 * @param operator the operator (see {@link #OPERATOR_NULL} and others
	 * @param params   optional parameters (one for >=, <=, two for the range, none for other operators)
	 */
	private byte[] getOpCode(final int opCode, final int operator, final Integer... params) {
        // 1 byte for opCode, 1 for operator, 1 for filter type (if parameters exists) and 2 for each parameter
		final int size = 2 + ((params.length > 0) ? 1 : 0) + params.length * 2;
		final byte[] data = new byte[size];

		// Write the operation code
		int offset = 0;
		data[offset++] = (byte) opCode;

		// Write the operator. This is always present but may be equal to OPERATOR_NULL
		data[offset++] = (byte) operator;

		// If parameters exists, append them. Parameters should be sorted from minimum to maximum.
        // Currently only one or two params are allowed
		if (params.length > 0) {
			// Our implementation use only sequence number as a filer type
			data[offset++] = FILTER_TYPE_SEQUENCE_NUMBER;

			for (final Integer i : params) {
				data[offset++] = (byte) (i & 0xFF);
				data[offset++] = (byte) ((i >> 8) & 0xFF);
			}
		}
		return data;
	}

	/**
	 * Returns all records as a sparse array where sequence number is the key.
	 *
	 * @return the records list
	 */
	public SparseArray<GlucoseRecord> getRecords() {
		return mRecords;
	}

	/**
	 * Clears the records list locally
	 */
	public void clear() {
		mRecords.clear();
		mCallbacks.onOperationCompleted(mBluetoothDevice);
	}

	/**
	 * Sends the request to obtain the last (most recent) record from glucose device. The data will
     * be returned to Glucose Measurement characteristic as a notification followed by Record Access
	 * Control Point indication with status code ({@link #RESPONSE_SUCCESS} or other in case of error.
	 */
	public void getLastRecord() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted(mBluetoothDevice);

		final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
		writeCharacteristic(characteristic, getOpCode(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_LAST_RECORD));
	}

	/**
	 * Sends the request to obtain the first (oldest) record from glucose device. The data will be
     * returned to Glucose Measurement characteristic as a notification followed by Record Access
     * Control Point indication with status code ({@link #RESPONSE_SUCCESS} or other in case of error.
	 */
	public void getFirstRecord() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted(mBluetoothDevice);

		final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
		writeCharacteristic(characteristic, getOpCode(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_FIRST_RECORD));
	}

	/**
	 * Sends the request to obtain all records from glucose device. Initially we want to notify
     * him/her about the number of the records so the {@link #OP_CODE_REPORT_NUMBER_OF_RECORDS}
     * is send. The data will be returned to Glucose Measurement characteristic as a notification
     * followed by Record Access Control Point indication with status code ({@link #RESPONSE_SUCCESS}
     * or other in case of error.
	 */
	public void getAllRecords() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted(mBluetoothDevice);

		final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
		writeCharacteristic(characteristic, getOpCode(OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_ALL_RECORDS));
	}

	/**
	 * Sends the request to obtain from the glucose device all records newer than the newest one
     * from local storage. The data will be returned to Glucose Measurement characteristic as a
     * notification followed by Record Access Control Point indication with status code
     * ({@link #RESPONSE_SUCCESS} or other in case of error.
	 * <p>
	 * Refresh button will not download records older than the oldest in the local memory.
     * E.g. if you have pressed Last and then Refresh, than it will try to get only newer records.
     * However, if there are no records, it will download all existing (using {@link #getAllRecords()}).
	 */
	public void refreshRecords() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		if (mRecords.size() == 0) {
			getAllRecords();
		} else {
			mCallbacks.onOperationStarted(mBluetoothDevice);

			// obtain the last sequence number
			final int sequenceNumber = mRecords.keyAt(mRecords.size() - 1) + 1;

			final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
			writeCharacteristic(characteristic, getOpCode(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_GREATER_THEN_OR_EQUAL, sequenceNumber));
			// Info:
			// Operators OPERATOR_LESS_THEN_OR_EQUAL and OPERATOR_RANGE are not supported by Nordic Semiconductor Glucose Service in SDK 4.4.2.
		}
	}

	/**
	 * Sends abort operation signal to the device
	 */
	public void abort() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		mAbort = true;
		final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
		writeCharacteristic(characteristic, getOpCode(OP_CODE_ABORT_OPERATION, OPERATOR_NULL));
	}

	/**
	 * Sends the request to delete all data from the device. A Record Access Control Point indication
     * with status code ({@link #RESPONSE_SUCCESS} (or other in case of error) will be send.
	 * <p>
	 * FIXME This method is not supported by Nordic Semiconductor Glucose Service in SDK 4.4.2.
	 */
	public void deleteAllRecords() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted(mBluetoothDevice);

		final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
		writeCharacteristic(characteristic, getOpCode(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_ALL_RECORDS));
	}
}
