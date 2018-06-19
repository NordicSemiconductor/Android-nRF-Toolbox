/*
 * Copyright (c) 2016, Nordic Semiconductor
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

package no.nordicsemi.android.nrftoolbox.cgms;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.SparseArray;

import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.Request;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.parser.CGMMeasurementParser;
import no.nordicsemi.android.nrftoolbox.parser.CGMSpecificOpsControlPointParser;
import no.nordicsemi.android.nrftoolbox.parser.RecordAccessControlPointParser;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class CGMSManager extends BleManager<CGMSManagerCallbacks> {
	private static final String TAG = "CGMSManager";

	/**
	 * Cycling Speed and Cadence service UUID
	 */
	public final static UUID CGMS_UUID = UUID.fromString("0000181F-0000-1000-8000-00805f9b34fb");
	private static final UUID CGM_MEASUREMENT_UUID = UUID.fromString("00002AA7-0000-1000-8000-00805f9b34fb");
	private static final UUID CGM_OPS_CONTROL_POINT_UUID = UUID.fromString("00002AAC-0000-1000-8000-00805f9b34fb");
	private final static int OP_CODE_START_SESSION = 26;
	/**
	 * Record Access Control Point characteristic UUID
	 */
	private final static UUID RACP_UUID = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb");

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

	private BluetoothGattCharacteristic mCGMMeasurementCharacteristic;
	private BluetoothGattCharacteristic mCGMOpsControlPointCharacteristic;
	private BluetoothGattCharacteristic mRecordAccessControlPointCharacteristic;

	private static CGMSManager managerInstance = null;
	private SparseArray<CGMSRecord> mRecords = new SparseArray<>();
	private boolean mAbort;
	private long mSessionStartTime;

	/**
	 * singleton implementation of HRSManager class
	 */
	public static synchronized CGMSManager getInstance(final Context context) {
		if (managerInstance == null) {
			managerInstance = new CGMSManager(context);
		}
		return managerInstance;
	}

	public CGMSManager(Context context) {
		super(context);
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
			requests.add(Request.newEnableNotificationsRequest(mCGMMeasurementCharacteristic));
			if (mCGMOpsControlPointCharacteristic != null) {
				mSessionStartTime = System.currentTimeMillis();
				requests.add(Request.newEnableIndicationsRequest(mCGMOpsControlPointCharacteristic));
				requests.add(Request.newWriteRequest(mCGMOpsControlPointCharacteristic, new byte[]{OP_CODE_START_SESSION}));
			}
			requests.add(Request.newEnableIndicationsRequest(mRecordAccessControlPointCharacteristic));
			return requests;
		}

		@Override
		protected boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(CGMS_UUID);
			if (service != null) {
				mCGMMeasurementCharacteristic = service.getCharacteristic(CGM_MEASUREMENT_UUID);
				mCGMOpsControlPointCharacteristic = service.getCharacteristic(CGM_OPS_CONTROL_POINT_UUID);
				mRecordAccessControlPointCharacteristic = service.getCharacteristic(RACP_UUID);
			}
			return mCGMMeasurementCharacteristic != null && mCGMOpsControlPointCharacteristic != null && mRecordAccessControlPointCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(CGMS_UUID);
			if (service != null) {
				mCGMOpsControlPointCharacteristic = service.getCharacteristic(CGM_OPS_CONTROL_POINT_UUID);
			}
			return mCGMOpsControlPointCharacteristic != null;
		}

		@Override
		public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
		}

		@Override
		protected void onDeviceDisconnected() {
			mCGMOpsControlPointCharacteristic = null;
			mCGMMeasurementCharacteristic = null;
			mRecordAccessControlPointCharacteristic = null;
		}

		@Override
		protected void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			if (characteristic.getUuid().equals(RACP_UUID)) {
				Logger.a(mLogSession, "\"" + RecordAccessControlPointParser.parse(characteristic) + "\" sent");
			} else { // uuid == CGM_OPS_CONTROL_POINT_UUID
				Logger.a(mLogSession, "\"" + CGMSpecificOpsControlPointParser.parse(characteristic) + "\" sent");
			}
		}

		@Override
		public void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			Logger.a(mLogSession, "\"" + CGMMeasurementParser.parse(characteristic) + "\" received");

			// CGM Measurement characteristic may have one or more CGM records
			int totalSize = characteristic.getValue().length;
			int offset = 0;
			while (offset < totalSize) {
				final int cgmSize = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
				final float cgmValue = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset + 2);
				final int sequenceNumber = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset + 4);
				final long timestamp = mSessionStartTime + (sequenceNumber * 60000L); // Sequence number is in minutes since Start Session

				//This will send callback to CGMSActivity when new concentration value is received from CGMS device
				final CGMSRecord cgmsRecord = new CGMSRecord(sequenceNumber, cgmValue, timestamp);
				mRecords.put(cgmsRecord.sequenceNumber, cgmsRecord);
				mCallbacks.onCGMValueReceived(gatt.getDevice(), cgmsRecord);

				offset += cgmSize;
			}
		}

		@Override
		protected void onCharacteristicIndicated(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			if (characteristic.getUuid().equals(RACP_UUID)) {
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
			} else { // uuid == CGM_OPS_CONTROL_POINT_UUID
				Logger.a(mLogSession, "\"" + CGMSpecificOpsControlPointParser.parse(characteristic) + "\" received");
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
	 * Returns a list of CGM records obtained from this device. The key in the array is the
	 */
	public SparseArray<CGMSRecord> getRecords() {
		return mRecords;
	}

	/**
	 * Clears the records list locally
	 */
	public void clear() {
		mRecords.clear();
		mCallbacks.onDatasetClear(mBluetoothDevice);
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
	 * Sends the request to obtain all records from glucose device. Initially we want to notify
	 * him/her about the number of the records so the {@link #OP_CODE_REPORT_NUMBER_OF_RECORDS}
	 * is send. The data will be returned to Glucose Measurement characteristic as a notification
	 * followed by Record Access Control Point indication with status code ({@link #RESPONSE_SUCCESS}
	 * or other in case of error.
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
			// Operators OPERATOR_GREATER_THEN_OR_EQUAL, OPERATOR_LESS_THEN_OR_EQUAL and OPERATOR_RANGE are not supported by the CGMS sample from SDK
			// The "Operation not supported" response will be received
		}
	}

	public void deleteAllRecords() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted(mBluetoothDevice);

		final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
		writeCharacteristic(characteristic, getOpCode(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_ALL_RECORDS));
	}
}

