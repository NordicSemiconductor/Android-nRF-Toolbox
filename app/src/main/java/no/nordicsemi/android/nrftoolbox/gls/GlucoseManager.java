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

import java.util.Calendar;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.SparseArray;

@SuppressWarnings("unused")
public class GlucoseManager implements BleManager<GlucoseManagerCallbacks> {
	private static final String TAG = "GlucoseManager";

	private GlucoseManagerCallbacks mCallbacks;
	private BluetoothGatt mBluetoothGatt;
	private Context mContext;
	private Handler mHandler;
	private boolean mAbort;

	private final SparseArray<GlucoseRecord> mRecords = new SparseArray<>();

	public final static UUID GLS_SERVICE_UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");
	public final static UUID BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
	/** Glucose Measurement characteristic */
	private final static UUID GM_CHARACTERISTIC = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb");
	/** Glucose Measurement Context characteristic */
	private final static UUID GM_CONTEXT_CHARACTERISTIC = UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb");
	/** Glucose Feature characteristic */
	private final static UUID GF_CHARACTERISTIC = UUID.fromString("00002A51-0000-1000-8000-00805f9b34fb");
	/** Record Access Control Point characteristic */
	private final static UUID RACP_CHARACTERISTIC = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb");
	/** Battery Level characteristic */
	private final static UUID BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
	/** Client configuration descriptor that will allow us to enable notifications and indications */
	private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

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
	 * The filter type is used for range operators ({@link #OPERATOR_LESS_THEN_OR_EQUAL}, {@link #OPERATOR_GREATER_THEN_OR_EQUAL}, {@link #OPERATOR_WITHING_RANGE}.<br/>
	 * The syntax of the operand is: [Filter Type][Minimum][Maximum].<br/>
	 * This filter selects the records by the sequence number.
	 */
	private final static int FILTER_TYPE_SEQUENCE_NUMBER = 1;
	/**
	 * The filter type is used for range operators ({@link #OPERATOR_LESS_THEN_OR_EQUAL}, {@link #OPERATOR_GREATER_THEN_OR_EQUAL}, {@link #OPERATOR_WITHING_RANGE}.<br/>
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

	private final static String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
	private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
	private final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
	private final static String ERROR_WRITE_CHARACTERISTIC = "Error on writing characteristic";
	private final static String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";

	private BluetoothGattCharacteristic mGlucoseMeasurementCharacteristic;
	private BluetoothGattCharacteristic mGlucoseFeatureCharacteristic;
	private BluetoothGattCharacteristic mGlucoseMeasurementContextCharacteristic;
	private BluetoothGattCharacteristic mRecordAccessControlPointCharacteristic;
	private BluetoothGattCharacteristic mBatteryLevelCharacteristic;

	private static GlucoseManager mInstance;

	/**
	 * Returns the singleton implementation of GlucoseManager
	 */
	public static GlucoseManager getGlucoseManager() {
		if (mInstance == null)
			mInstance = new GlucoseManager();
		return mInstance;
	}

	/**
	 * Callbacks for activity {@link GlucoseActivity} that implements {@link GlucoseManagerCallbacks} interface activity use this method to register itself for receiving callbacks
	 */
	@Override
	public void setGattCallbacks(final GlucoseManagerCallbacks callbacks) {
		mCallbacks = callbacks;
	}

	/**
	 * Returns all records as a sparse array where sequence number is the key.
	 * 
	 * @return the records list
	 */
	public SparseArray<GlucoseRecord> getRecords() {
		return mRecords;
	}

	@Override
	public void connect(final Context context, final BluetoothDevice device) {
		if (mHandler == null)
			mHandler = new Handler();
		mContext = context;

		// Register bonding broadcast receiver
		final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		context.registerReceiver(mBondingBroadcastReceiver, filter);

		mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
	}

	/**
	 * Disable HR notification first and then disconnect to HR device
	 */
	@Override
	public void disconnect() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
		}
	}

	/**
	 * Clears the records list locally
	 */
	public void clear() {
		mRecords.clear();
		mCallbacks.onDatasetChanged();
	}

	/**
	 * Sends the request to obtain the last (most recent) record from glucose device. The data will be returned to Glucose Measurement characteristic as a notification followed by Record Access
	 * Control Point indication with status code ({@link #RESPONSE_SUCCESS} or other in case of error.
	 */
	public void getLastRecord() {
		if (mBluetoothGatt == null || mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted();

		final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
		setOpCode(characteristic, OP_CODE_REPORT_STORED_RECORDS, OPERATOR_LAST_RECORD);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	/**
	 * Sends the request to obtain the first (oldest) record from glucose device. The data will be returned to Glucose Measurement characteristic as a notification followed by Record Access Control
	 * Point indication with status code ({@link #RESPONSE_SUCCESS} or other in case of error.
	 */
	public void getFirstRecord() {
		if (mBluetoothGatt == null || mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted();

		final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
		setOpCode(characteristic, OP_CODE_REPORT_STORED_RECORDS, OPERATOR_FIRST_RECORD);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	/**
	 * Sends the request to obtain all records from glucose device. Initially we want to notify him/her about the number of the records so the {@link #OP_CODE_REPORT_NUMBER_OF_RECORDS} is send. The
	 * data will be returned to Glucose Measurement characteristic as a notification followed by Record Access Control Point indication with status code ({@link #RESPONSE_SUCCESS} or other in case of
	 * error.
	 */
	public void getAllRecords() {
		if (mBluetoothGatt == null || mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted();

		final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
		setOpCode(characteristic, OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_ALL_RECORDS);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	/**
	 * Sends the request to obtain from the glucose device all records newer than the newest one from local storage. The data will be returned to Glucose Measurement characteristic as a notification
	 * followed by Record Access Control Point indication with status code ({@link #RESPONSE_SUCCESS} or other in case of error.
	 * <p>
	 * Refresh button will not download records older than the oldest in the local memory. F.e. if you have pressed Last and then Refresh, than it will try to get only newer records. However if there
	 * are no records, it will download all existing (using {@link #getAllRecords()}).
	 * </p>
	 */
	public void refreshRecords() {
		if (mBluetoothGatt == null || mRecordAccessControlPointCharacteristic == null)
			return;

		if (mRecords.size() == 0) {
			getAllRecords();
		} else {
			mCallbacks.onOperationStarted();

			// obtain the last sequence number
			final int sequenceNumber = mRecords.keyAt(mRecords.size() - 1) + 1;

			final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
			setOpCode(characteristic, OP_CODE_REPORT_STORED_RECORDS, OPERATOR_GREATER_THEN_OR_EQUAL, sequenceNumber);
			mBluetoothGatt.writeCharacteristic(characteristic);
			// Info:
			// Operators OPERATOR_LESS_THEN_OR_EQUAL and OPERATOR_RANGE are not supported by Nordic Semiconductor Glucose Service in SDK 4.4.2.
		}
	}

	/**
	 * Sends abort operation signal to the device
	 */
	public void abort() {
		if (mBluetoothGatt == null || mRecordAccessControlPointCharacteristic == null)
			return;

		mAbort = true;
		final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
		setOpCode(characteristic, OP_CODE_ABORT_OPERATION, OPERATOR_NULL);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	/**
	 * Sends the request to delete all data from the device. A Record Access Control Point indication with status code ({@link #RESPONSE_SUCCESS} (or other in case of error) will be send.
	 * 
	 * @FIXME This method is not supported by Nordic Semiconductor Glucose Service in SDK 4.4.2.
	 */
	public void deleteAllRecords() {
		if (mBluetoothGatt == null || mRecordAccessControlPointCharacteristic == null)
			return;

		clear();
		mCallbacks.onOperationStarted();

		final BluetoothGattCharacteristic characteristic = mRecordAccessControlPointCharacteristic;
		setOpCode(characteristic, OP_CODE_DELETE_STORED_RECORDS, OPERATOR_ALL_RECORDS);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	private BroadcastReceiver mBondingBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
			final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

			DebugLogger.d(TAG, "Bond state changed for: " + device.getAddress() + " new state: " + bondState + " previous: " + previousBondState);

			// skip other devices
			if (!device.getAddress().equals(mBluetoothGatt.getDevice().getAddress()))
				return;

			if (bondState == BluetoothDevice.BOND_BONDING) {
				mCallbacks.onBondingRequired();
			} else if (bondState == BluetoothDevice.BOND_BONDED) {
				// We've read Battery Level, now let'so enable notifications and indication
				enableGlucoseMeasurementNotification(mBluetoothGatt);
				mCallbacks.onBonded();
			}
		}
	};

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving notification, etc
	 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					mCallbacks.onDeviceConnected();
					// start discovering services
					gatt.discoverServices();
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					mCallbacks.onDeviceDisconnected();
					gatt.close();
				}
			} else {
				DebugLogger.e(TAG, "onConnectionStateChange error " + status);
				mCallbacks.onError(ERROR_CONNECTION_STATE_CHANGE, status);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				for (BluetoothGattService service : gatt.getServices()) {
					if (GLS_SERVICE_UUID.equals(service.getUuid())) {
						mGlucoseMeasurementCharacteristic = service.getCharacteristic(GM_CHARACTERISTIC);
						mGlucoseMeasurementContextCharacteristic = service.getCharacteristic(GM_CONTEXT_CHARACTERISTIC);
						mGlucoseFeatureCharacteristic = service.getCharacteristic(GF_CHARACTERISTIC);
						mRecordAccessControlPointCharacteristic = service.getCharacteristic(RACP_CHARACTERISTIC);
					} else if (BATTERY_SERVICE.equals(service.getUuid())) {
						mBatteryLevelCharacteristic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC);
					}
				}
				// Validate the device for required characteristics
				if (mGlucoseMeasurementCharacteristic == null || mRecordAccessControlPointCharacteristic == null) {
					mCallbacks.onDeviceNotSupported();
					gatt.disconnect();
					return;
				}
				mCallbacks.onServicesDiscovered(mGlucoseMeasurementContextCharacteristic != null);

				// We have discovered services, let's start notifications and indications, one by one: read battery, enable GM, GMP (if exists) and RACP
				if (mBatteryLevelCharacteristic != null) {
					readBatteryLevel(gatt);
				} else {
					// this characteristic is mandatory
					enableGlucoseMeasurementNotification(gatt);
				}
			} else {
				DebugLogger.e(TAG, "onServicesDiscovered error " + status);
				mCallbacks.onError(ERROR_DISCOVERY_SERVICE, status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (BATTERY_LEVEL_CHARACTERISTIC.equals(characteristic.getUuid())) {
					final int batteryValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
					mCallbacks.onBatteryValueReceived(batteryValue);

					// We've read Battery Level, now let'so enable ICP notifications or BPM indications 
					enableGlucoseMeasurementNotification(gatt);
				}
			} else {
				DebugLogger.e(TAG, "onCharacteristicRead error " + status);
				mCallbacks.onError(ERROR_DISCOVERY_SERVICE, status);
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (GM_CHARACTERISTIC.equals(descriptor.getCharacteristic().getUuid())) {
					mCallbacks.onGlucoseMeasurementNotificationEnabled();

					if (mGlucoseMeasurementContextCharacteristic != null) {
						enableGlucoseMeasurementContextNotification(gatt);
					} else {
						enableRecordAccessControlPointIndication(gatt);
					}
				}

				if (GM_CONTEXT_CHARACTERISTIC.equals(descriptor.getCharacteristic().getUuid())) {
					mCallbacks.onGlucoseMeasurementContextNotificationEnabled();
					enableRecordAccessControlPointIndication(gatt);
				}

				if (RACP_CHARACTERISTIC.equals(descriptor.getCharacteristic().getUuid())) {
					mCallbacks.onRecordAccessControlPointIndicationsEnabled();
				}
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					DebugLogger.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					mCallbacks.onError(ERROR_AUTH_ERROR_WHILE_BONDED, status);
				}
			} else {
				DebugLogger.e(TAG, "onDescriptorWrite error " + status);
				mCallbacks.onError(ERROR_WRITE_DESCRIPTOR, status);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			final UUID uuid = characteristic.getUuid();
			if (GM_CHARACTERISTIC.equals(uuid)) {
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
				final int month = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2) + 1; // months are 1-based
				final int day = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 3);
				final int hours = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 4);
				final int minutes = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 5);
				final int seconds = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 6);
				offset += 7;

				final Calendar calendar = Calendar.getInstance();
				calendar.set(year, month, day, hours, minutes, seconds);
				record.time = calendar;

				if (timeOffsetPresent) {
					// time offset is ignored in the current release
					record.timeOffset = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
					offset += 2;
				}

				if (typeAndLocationPresent) {
					record.glucoseConcentration = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset);
					record.unit = concentrationUnit;
					final int typeAndLocation = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 2);
					record.type = (typeAndLocation & 0xF0) >> 4; // TODO this way or around?
					record.sampleLocation = (typeAndLocation & 0x0F);
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
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// insert the new record to storage
						mRecords.put(record.sequenceNumber, record);

						// if there is no context information following the measurement data, notify callback about the new record
						if (!contextInfoFollows)
							mCallbacks.onDatasetChanged();
					}
				});
			} else if (GM_CONTEXT_CHARACTERISTIC.equals(uuid)) {
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
				mCallbacks.onDatasetChanged();
			} else { // Record Access Control Point characteristic
				int offset = 0;
				final int opCode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
				offset += 2; // skip the operator

				if (opCode == OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE) {
					// We've obtained the number of all records 
					final int number = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);

					mCallbacks.onNumberOfRecordsRequested(number);

					// Request the records
					final BluetoothGattCharacteristic racpCharacteristic = mRecordAccessControlPointCharacteristic;
					setOpCode(racpCharacteristic, OP_CODE_REPORT_STORED_RECORDS, OPERATOR_ALL_RECORDS);
					mBluetoothGatt.writeCharacteristic(racpCharacteristic);
				} else if (opCode == OP_CODE_RESPONSE_CODE) {
					final int requestedOpCode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
					final int responseCode = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1);
					DebugLogger.d(TAG, "Response result for: " + requestedOpCode + " is: " + responseCode);

					switch (responseCode) {
					case RESPONSE_SUCCESS:
						if (!mAbort)
							mCallbacks.onOperationCompleted();
						else
							mCallbacks.onOperationAborted();
						break;
					case RESPONSE_NO_RECORDS_FOUND:
						mCallbacks.onOperationCompleted();
						break;
					case RESPONSE_OP_CODE_NOT_SUPPORTED:
						mCallbacks.onOperationNotSupported();
						break;
					case RESPONSE_PROCEDURE_NOT_COMPLETED:
					case RESPONSE_ABORT_UNSUCCESSFUL:
					default:
						mCallbacks.onOperationFailed();
						break;
					}
					mAbort = false;
				}
			}
		}

		/**
		 * Reads battery level on the device
		 */
		private void readBatteryLevel(final BluetoothGatt gatt) {
			DebugLogger.d(TAG, "readBatteryLevel()");
			gatt.readCharacteristic(mBatteryLevelCharacteristic);
		}
	};

	/**
	 * Enabling notification on Glucose Measurement Characteristic
	 */
	private void enableGlucoseMeasurementNotification(final BluetoothGatt gatt) {
		DebugLogger.d(TAG, "enableGlucoseMeasurementNotification()");
		gatt.setCharacteristicNotification(mGlucoseMeasurementCharacteristic, true);
		final BluetoothGattDescriptor descriptor = mGlucoseMeasurementCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		gatt.writeDescriptor(descriptor);
	}

	/**
	 * Enabling notification on Glucose Measurement Context Characteristic. This characteristic is optional
	 */
	private void enableGlucoseMeasurementContextNotification(final BluetoothGatt gatt) {
		DebugLogger.d(TAG, "enableGlucoseMeasurementContextNotification()");
		gatt.setCharacteristicNotification(mGlucoseMeasurementContextCharacteristic, true);
		final BluetoothGattDescriptor descriptor = mGlucoseMeasurementContextCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		gatt.writeDescriptor(descriptor);
	}

	/**
	 * Enabling indications on Record Access Control Point Characteristic
	 */
	private void enableRecordAccessControlPointIndication(final BluetoothGatt gatt) {
		DebugLogger.d(TAG, "enableGlucoseMeasurementContextNotification()");
		gatt.setCharacteristicNotification(mRecordAccessControlPointCharacteristic, true);
		final BluetoothGattDescriptor descriptor = mRecordAccessControlPointCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
		gatt.writeDescriptor(descriptor);
	}

	/**
	 * Writes given operation parameters to the characteristic
	 * 
	 * @param characteristic
	 *            the characteristic to write. This must be the Record Access Control Point characteristic
	 * @param opCode
	 *            the operation code
	 * @param operator
	 *            the operator (see {@link #OPERATOR_NULL} and others
	 * @param params
	 *            optional parameters (one for >=, <=, two for the range, none for other operators)
	 */
	private void setOpCode(final BluetoothGattCharacteristic characteristic, final int opCode, final int operator, final Integer... params) {
		final int size = 2 + ((params.length > 0) ? 1 : 0) + params.length * 2; // 1 byte for opCode, 1 for operator, 1 for filter type (if parameters exists) and 2 for each parameter
		characteristic.setValue(new byte[size]);

		// write the operation code
		int offset = 0;
		characteristic.setValue(opCode, BluetoothGattCharacteristic.FORMAT_UINT8, offset);
		offset += 1;

		// write the operator. This is always present but may be equal to OPERATOR_NULL
		characteristic.setValue(operator, BluetoothGattCharacteristic.FORMAT_UINT8, offset);
		offset += 1;

		// if parameters exists, append them. Parameters should be sorted from minimum to maximum. Currently only one or two params are allowed
		if (params.length > 0) {
			// our implementation use only sequence number as a filer type
			characteristic.setValue(FILTER_TYPE_SEQUENCE_NUMBER, BluetoothGattCharacteristic.FORMAT_UINT8, offset);
			offset += 1;

			for (final Integer i : params) {
				characteristic.setValue(i, BluetoothGattCharacteristic.FORMAT_UINT16, offset);
				offset += 2;
			}
		}
	}

	@Override
	public void closeBluetoothGatt() {
		try {
			mContext.unregisterReceiver(mBondingBroadcastReceiver);
		} catch (Exception e) {
			// the receiver must have been not registered or unregistered before
		}

		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mRecords.clear();
			mGlucoseMeasurementCharacteristic = null;
			mGlucoseMeasurementContextCharacteristic = null;
			mGlucoseFeatureCharacteristic = null;
			mRecordAccessControlPointCharacteristic = null;
			mBatteryLevelCharacteristic = null;
			mBluetoothGatt = null;
		}
	}
}
