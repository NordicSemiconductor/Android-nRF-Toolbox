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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.util.UUID;

import no.nordicsemi.android.ble.common.callback.RecordAccessControlPointDataCallback;
import no.nordicsemi.android.ble.common.callback.cgm.CGMFeatureDataCallback;
import no.nordicsemi.android.ble.common.callback.cgm.CGMSpecificOpsControlPointDataCallback;
import no.nordicsemi.android.ble.common.callback.cgm.CGMStatusDataCallback;
import no.nordicsemi.android.ble.common.callback.cgm.ContinuousGlucoseMeasurementDataCallback;
import no.nordicsemi.android.ble.common.data.RecordAccessControlPointData;
import no.nordicsemi.android.ble.common.data.cgm.CGMSpecificOpsControlPointData;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.battery.BatteryManager;
import no.nordicsemi.android.nrftoolbox.parser.CGMMeasurementParser;
import no.nordicsemi.android.nrftoolbox.parser.CGMSpecificOpsControlPointParser;
import no.nordicsemi.android.nrftoolbox.parser.RecordAccessControlPointParser;

public class CGMSManager extends BatteryManager<CGMSManagerCallbacks> {
	/** Cycling Speed and Cadence service UUID. */
	public static final UUID CGMS_UUID = UUID.fromString("0000181F-0000-1000-8000-00805f9b34fb");
	private static final UUID CGM_STATUS_UUID = UUID.fromString("00002AA9-0000-1000-8000-00805f9b34fb");
	private static final UUID CGM_FEATURE_UUID = UUID.fromString("00002AA8-0000-1000-8000-00805f9b34fb");
	private static final UUID CGM_MEASUREMENT_UUID = UUID.fromString("00002AA7-0000-1000-8000-00805f9b34fb");
	private static final UUID CGM_OPS_CONTROL_POINT_UUID = UUID.fromString("00002AAC-0000-1000-8000-00805f9b34fb");
	/** Record Access Control Point characteristic UUID. */
	private static final UUID RACP_UUID = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic mCGMStatusCharacteristic;
	private BluetoothGattCharacteristic mCGMFeatureCharacteristic;
	private BluetoothGattCharacteristic mCGMMeasurementCharacteristic;
	private BluetoothGattCharacteristic mCGMSpecificOpsControlPointCharacteristic;
	private BluetoothGattCharacteristic mRecordAccessControlPointCharacteristic;

	private SparseArray<CGMSRecord> mRecords = new SparseArray<>();

	/** A flag set to true if the remote device supports E2E CRC. */
	private boolean mSecured;
	/**
	 * A flag set when records has been requested using RACP. This is to distinguish CGM packets
	 * received as continuous measurements or requested.
	 */
	private boolean mRecordAccessRequestInProgress;
	/**
	 * The timestamp when the session has started. This is needed to display the user facing
	 * times of samples.
	 */
	private long mSessionStartTime;

	CGMSManager(final Context context) {
		super(context);
	}

	@NonNull
	@Override
	protected BatteryManagerGattCallback getGattCallback() {
		return mGattCallback;
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery,
	 * receiving notification, etc.
	 */
	private final BatteryManagerGattCallback mGattCallback = new BatteryManagerGattCallback() {

		@Override
		protected void initialize() {
			// Enable Battery service
			super.initialize();

			// Read CGM Feature characteristic, mainly to see if the device supports E2E CRC.
			// This is not supported in the experimental CGMS from the SDK.
			readCharacteristic(mCGMFeatureCharacteristic)
					.with(new CGMFeatureDataCallback() {
						@Override
						public void onContinuousGlucoseMonitorFeaturesReceived(@NonNull final BluetoothDevice device, @NonNull final CGMFeatures features,
																			   final int type, final int sampleLocation, final boolean secured) {
							mSecured = features.e2eCrcSupported;
							log(LogContract.Log.Level.APPLICATION, "E2E CRC feature " + (mSecured ? "supported" : "not supported"));
						}
					})
					.fail((device, status) -> log(Log.WARN, "Could not read CGM Feature characteristic"))
					.enqueue();

			// Check if the session is already started. This is not supported in the experimental CGMS from the SDK.
			readCharacteristic(mCGMStatusCharacteristic)
					.with(new CGMStatusDataCallback() {
						@Override
						public void onContinuousGlucoseMonitorStatusChanged(@NonNull final BluetoothDevice device, @NonNull final CGMStatus status, final int timeOffset, final boolean secured) {
							if (!status.sessionStopped) {
								mSessionStartTime = System.currentTimeMillis() - timeOffset * 60000L;
								log(LogContract.Log.Level.APPLICATION, "Session already started");
							}
						}
					})
					.fail((device, status) -> log(Log.WARN, "Could not read CGM Status characteristic"))
					.enqueue();

			// Set notification and indication callbacks
			setNotificationCallback(mCGMMeasurementCharacteristic)
					.with(new ContinuousGlucoseMeasurementDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + CGMMeasurementParser.parse(data) + "\" received");
							super.onDataReceived(device, data);
						}

						@Override
						public void onContinuousGlucoseMeasurementReceived(@NonNull final BluetoothDevice device,
                                                                           final float glucoseConcentration,
                                                                           @Nullable final Float cgmTrend,
                                                                           @Nullable final Float cgmQuality,
                                                                           final CGMStatus status,
                                                                           final int timeOffset,
                                                                           final boolean secured) {
							// If the CGM Status characteristic has not been read and the session was already started before,
							// estimate the Session Start Time by subtracting timeOffset minutes from the current timestamp.
							if (mSessionStartTime == 0 && !mRecordAccessRequestInProgress) {
								mSessionStartTime = System.currentTimeMillis() - timeOffset * 60000L;
							}

							// Calculate the sample timestamp based on the Session Start Time
							final long timestamp = mSessionStartTime + (timeOffset * 60000L); // Sequence number is in minutes since Start Session

							final CGMSRecord record = new CGMSRecord(timeOffset, glucoseConcentration, timestamp);
							mRecords.put(record.sequenceNumber, record);
							mCallbacks.onCGMValueReceived(device, record);
						}

						@Override
						public void onContinuousGlucoseMeasurementReceivedWithCrcError(@NonNull final BluetoothDevice device,
                                                                                       @NonNull final Data data) {
							log(Log.WARN, "Continuous Glucose Measurement record received with CRC error");
						}
					});

			setIndicationCallback(mCGMSpecificOpsControlPointCharacteristic)
					.with(new CGMSpecificOpsControlPointDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + CGMSpecificOpsControlPointParser.parse(data) + "\" received");
							super.onDataReceived(device, data);
						}

						@Override
						public void onCGMSpecificOpsOperationCompleted(@NonNull final BluetoothDevice device,
                                                                       final int requestCode, final boolean secured) {
							switch (requestCode) {
								case CGM_OP_CODE_START_SESSION:
									mSessionStartTime = System.currentTimeMillis();
									break;
								case CGM_OP_CODE_STOP_SESSION:
									mSessionStartTime = 0;
									break;
							}
						}

						@SuppressWarnings("StatementWithEmptyBody")
                        @Override
						public void onCGMSpecificOpsOperationError(@NonNull final BluetoothDevice device,
                                                                   final int requestCode, final int errorCode,
                                                                   final boolean secured) {
							switch (requestCode) {
								case CGM_OP_CODE_START_SESSION:
									if (errorCode == CGM_ERROR_PROCEDURE_NOT_COMPLETED) {
										// Session was already started before.
										// Looks like the CGM Status characteristic has not been read,
                                        // otherwise we would have got the Session Start Time before.
										// The Session Start Time will be calculated when a next CGM
                                        // packet is received based on it's Time Offset.
									}
								case CGM_OP_CODE_STOP_SESSION:
									mSessionStartTime = 0;
									break;
							}
						}

						@Override
						public void onCGMSpecificOpsResponseReceivedWithCrcError(@NonNull final BluetoothDevice device,
                                                                                 @NonNull final Data data) {
							log(Log.ERROR, "Request failed: CRC error");
						}
					});

			setIndicationCallback(mRecordAccessControlPointCharacteristic)
					.with(new RecordAccessControlPointDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" received");
							super.onDataReceived(device, data);
						}

						@Override
						public void onRecordAccessOperationCompleted(@NonNull final BluetoothDevice device, final int requestCode) {
							switch (requestCode) {
								case RACP_OP_CODE_ABORT_OPERATION:
									mCallbacks.onOperationAborted(device);
									break;
								default:
									mRecordAccessRequestInProgress = false;
									mCallbacks.onOperationCompleted(device);
									break;
							}
						}

						@Override
						public void onRecordAccessOperationCompletedWithNoRecordsFound(@NonNull final BluetoothDevice device,
                                                                                       final int requestCode) {
							mRecordAccessRequestInProgress = false;
							mCallbacks.onOperationCompleted(device);
						}

						@Override
						public void onNumberOfRecordsReceived(@NonNull final BluetoothDevice device, final int numberOfRecords) {
							mCallbacks.onNumberOfRecordsRequested(device, numberOfRecords);
							if (numberOfRecords > 0) {
								if (mRecords.size() > 0) {
									final int sequenceNumber = mRecords.keyAt(mRecords.size() - 1) + 1;
									writeCharacteristic(mRecordAccessControlPointCharacteristic,
                                            RecordAccessControlPointData.reportStoredRecordsGreaterThenOrEqualTo(sequenceNumber))
											.enqueue();
								} else {
									writeCharacteristic(mRecordAccessControlPointCharacteristic,
                                            RecordAccessControlPointData.reportAllStoredRecords())
											.enqueue();
								}
							} else {
								mRecordAccessRequestInProgress = false;
								mCallbacks.onOperationCompleted(device);
							}
						}

						@Override
						public void onRecordAccessOperationError(@NonNull final BluetoothDevice device,
                                                                 final int requestCode, final int errorCode) {
							log(Log.WARN, "Record Access operation failed (error " + errorCode + ")");
							if (errorCode == RACP_ERROR_OP_CODE_NOT_SUPPORTED) {
								mCallbacks.onOperationNotSupported(device);
							} else {
								mCallbacks.onOperationFailed(device);
							}
						}
					});

			// Enable notifications and indications
			enableNotifications(mCGMMeasurementCharacteristic)
					.fail((device, status) -> log(Log.WARN, "Failed to enable Continuous Glucose Measurement notifications (" + status + ")"))
					.enqueue();
			enableIndications(mCGMSpecificOpsControlPointCharacteristic)
					.fail((device, status) -> log(Log.WARN, "Failed to enable CGM Specific Ops Control Point indications notifications (" + status + ")"))
					.enqueue();
			enableIndications(mRecordAccessControlPointCharacteristic)
					.fail((device, status) -> log(Log.WARN, "Failed to enabled Record Access Control Point indications (error " + status + ")"))
					.enqueue();

			// Start Continuous Glucose session if hasn't been started before
			if (mSessionStartTime == 0L) {
				writeCharacteristic(mCGMSpecificOpsControlPointCharacteristic, CGMSpecificOpsControlPointData.startSession(mSecured))
						.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + CGMSpecificOpsControlPointParser.parse(data) + "\" sent"))
						.fail((device, status) -> log(LogContract.Log.Level.ERROR, "Failed to start session (error " + status + ")"))
						.enqueue();
			}
		}

		@Override
		protected boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(CGMS_UUID);
			if (service != null) {
				mCGMStatusCharacteristic = service.getCharacteristic(CGM_STATUS_UUID);
				mCGMFeatureCharacteristic = service.getCharacteristic(CGM_FEATURE_UUID);
				mCGMMeasurementCharacteristic = service.getCharacteristic(CGM_MEASUREMENT_UUID);
				mCGMSpecificOpsControlPointCharacteristic = service.getCharacteristic(CGM_OPS_CONTROL_POINT_UUID);
				mRecordAccessControlPointCharacteristic = service.getCharacteristic(RACP_UUID);
			}
			return mCGMMeasurementCharacteristic != null
                    && mCGMSpecificOpsControlPointCharacteristic != null
                    && mRecordAccessControlPointCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			super.onDeviceDisconnected();
			mCGMStatusCharacteristic = null;
			mCGMFeatureCharacteristic = null;
			mCGMMeasurementCharacteristic = null;
			mCGMSpecificOpsControlPointCharacteristic = null;
			mRecordAccessControlPointCharacteristic = null;
		}
	};

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
		mCallbacks.onDatasetCleared(getBluetoothDevice());
	}

	/**
	 * Sends the request to obtain the last (most recent) record from glucose device.
     * The data will be returned to Glucose Measurement characteristic as a notification followed by
     * Record Access Control Point indication with status code Success or other in case of error.
	 */
	public void getLastRecord() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted(getBluetoothDevice());
		mRecordAccessRequestInProgress = true;
		writeCharacteristic(mRecordAccessControlPointCharacteristic, RecordAccessControlPointData.reportLastStoredRecord())
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
				.enqueue();
	}

	/**
	 * Sends the request to obtain the first (oldest) record from glucose device.
	 * The data will be returned to Glucose Measurement characteristic as a notification followed by
     * Record Access Control Point indication with status code Success or other in case of error.
	 */
	public void getFirstRecord() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted(getBluetoothDevice());
		mRecordAccessRequestInProgress = true;
		writeCharacteristic(mRecordAccessControlPointCharacteristic, RecordAccessControlPointData.reportFirstStoredRecord())
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
				.enqueue();
	}

	/**
	 * Sends abort operation signal to the device.
	 */
	public void abort() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		writeCharacteristic(mRecordAccessControlPointCharacteristic, RecordAccessControlPointData.abortOperation())
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
				.enqueue();
	}

	/**
	 * Sends the request to obtain all records from glucose device. Initially we want to notify the
	 * user about the number of the records so the Report Number of Stored Records request is send.
	 * The data will be returned to Glucose Measurement characteristic as a notification followed by
	 * Record Access Control Point indication with status code Success or other in case of error.
	 */
	public void getAllRecords() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted(getBluetoothDevice());
		mRecordAccessRequestInProgress = true;
		writeCharacteristic(mRecordAccessControlPointCharacteristic, RecordAccessControlPointData.reportNumberOfAllStoredRecords())
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
				.enqueue();
	}

	/**
	 * Sends the request to obtain all records from glucose device. Initially we want to notify the
	 * user about the number of the records so the Report Number of Stored Records request is send.
	 * The data will be returned to Glucose Measurement characteristic as a notification followed by
	 * Record Access Control Point indication with status code Success or other in case of error.
	 */
	public void refreshRecords() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		if (mRecords.size() == 0) {
			getAllRecords();
		} else {
			mCallbacks.onOperationStarted(getBluetoothDevice());

			// Obtain the last sequence number
			final int sequenceNumber = mRecords.keyAt(mRecords.size() - 1) + 1;
			mRecordAccessRequestInProgress = true;
			writeCharacteristic(mRecordAccessControlPointCharacteristic, RecordAccessControlPointData.reportStoredRecordsGreaterThenOrEqualTo(sequenceNumber))
					.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
					.enqueue();
			// Info:
			// Operators OPERATOR_GREATER_THEN_OR_EQUAL, OPERATOR_LESS_THEN_OR_EQUAL and OPERATOR_RANGE are not supported by the CGMS sample from SDK
			// The "Operation not supported" response will be received
		}
	}

	/**
	 * Sends the request to remove all stored records from the Continuous Glucose Monitor device.
	 * This feature is not supported by the CGMS sample from the SDK, so monitor will answer with
	 * the Op Code Not Supported error.
	 */
	public void deleteAllRecords() {
		if (mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted(getBluetoothDevice());
		writeCharacteristic(mRecordAccessControlPointCharacteristic, RecordAccessControlPointData.deleteAllStoredRecords())
				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
				.enqueue();
	}
}

