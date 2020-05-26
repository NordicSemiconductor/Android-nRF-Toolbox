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
package no.nordicsemi.android.nrftoolbox.template;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.battery.BatteryManager;
import no.nordicsemi.android.nrftoolbox.parser.TemplateParser;
import no.nordicsemi.android.nrftoolbox.template.callback.TemplateDataCallback;

/**
 * Modify to template manager to match your requirements.
 * The TemplateManager extends {@link BatteryManager}, but it may easily extend {@link BleManager}
 * instead if you don't need Battery Service support. If not, also modify the
 * {@link TemplateManagerCallbacks} to extend {@link no.nordicsemi.android.ble.BleManagerCallbacks}
 * and replace BatteryManagerGattCallback to BleManagerGattCallback in this class.
 */
public class TemplateManager extends BatteryManager<TemplateManagerCallbacks> {
	// TODO Replace the services and characteristics below to match your device.
	/**
	 * The service UUID.
	 */
	static final UUID SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb"); // Heart Rate service
	/**
	 * A UUID of a characteristic with notify property.
	 */
	private static final UUID MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb"); // Heart Rate Measurement
	/**
	 * A UUID of a characteristic with read property.
	 */
	private static final UUID READABLE_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb"); // Body Sensor Location
	/**
	 * Some other service UUID.
	 */
	private static final UUID OTHER_SERVICE_UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb"); // Generic Access service
	/**
	 * A UUID of a characteristic with write property.
	 */
	private static final UUID WRITABLE_CHARACTERISTIC_UUID = UUID.fromString("00002A00-0000-1000-8000-00805f9b34fb"); // Device Name

	// TODO Add more services and characteristics references.
	private BluetoothGattCharacteristic requiredCharacteristic, deviceNameCharacteristic, optionalCharacteristic;

	public TemplateManager(final Context context) {
		super(context);
	}

	@NonNull
	@Override
	protected BatteryManagerGattCallback getGattCallback() {
		return new TemplateManagerGattCallback();
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery,
	 * receiving indication, etc.
	 */
	private class TemplateManagerGattCallback extends BatteryManagerGattCallback {

		@Override
		protected void initialize() {
			// Initialize the Battery Manager. It will enable Battery Level notifications.
			// Remove it if you don't need this feature.
			super.initialize();

			// TODO Initialize your manager here.
			// Initialization is done once, after the device is connected. Usually it should
			// enable notifications or indications on some characteristics, write some data or
			// read some features / version.
			// After the initialization is complete, the onDeviceReady(...) method will be called.

			// Increase the MTU
			requestMtu(43)
					.with((device, mtu) -> log(LogContract.Log.Level.APPLICATION, "MTU changed to " + mtu))
					.done(device -> {
						// You may do some logic in here that should be done when the request finished successfully.
						// In case of MTU this method is called also when the MTU hasn't changed, or has changed
						// to a different (lower) value. Use .with(...) to get the MTU value.
					})
					.fail((device, status) -> log(Log.WARN, "MTU change not supported"))
					.enqueue();

			// Set notification callback
			setNotificationCallback(requiredCharacteristic)
					// This callback will be called each time the notification is received
					.with(new TemplateDataCallback() {
						@Override
						public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(LogContract.Log.Level.APPLICATION, TemplateParser.parse(data));
							super.onDataReceived(device, data);
						}

						@Override
						public void onSampleValueReceived(@NonNull final BluetoothDevice device, final int value) {
							// Let's lass received data to the service
							mCallbacks.onSampleValueReceived(device, value);
						}

						@Override
						public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
							log(Log.WARN, "Invalid data received: " + data);
						}
					});

			// Enable notifications
			enableNotifications(requiredCharacteristic)
					// Method called after the data were sent (data will contain 0x0100 in this case)
					.with((device, data) -> log(Log.DEBUG, "Data sent: " + data))
					// Method called when the request finished successfully. This will be called after .with(..) callback
					.done(device -> log(LogContract.Log.Level.APPLICATION, "Notifications enabled successfully"))
					// Methods called in case of an error, for example when the characteristic does not have Notify property
					.fail((device, status) -> log(Log.WARN, "Failed to enable notifications"))
					.enqueue();
		}

		@Override
		protected boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			// TODO Initialize required characteristics.
			// It should return true if all has been discovered (that is that device is supported).
			final BluetoothGattService service = gatt.getService(SERVICE_UUID);
			if (service != null) {
				requiredCharacteristic = service.getCharacteristic(MEASUREMENT_CHARACTERISTIC_UUID);
			}
			final BluetoothGattService otherService = gatt.getService(OTHER_SERVICE_UUID);
			if (otherService != null) {
				deviceNameCharacteristic = otherService.getCharacteristic(WRITABLE_CHARACTERISTIC_UUID);
			}
			return requiredCharacteristic != null && deviceNameCharacteristic != null;
		}

		@Override
		protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
			// Initialize Battery characteristic
			super.isOptionalServiceSupported(gatt);

			// TODO If there are some optional characteristics, initialize them there.
			final BluetoothGattService service = gatt.getService(SERVICE_UUID);
			if (service != null) {
				optionalCharacteristic = service.getCharacteristic(READABLE_CHARACTERISTIC_UUID);
			}
			return optionalCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			// Release Battery Service
			super.onDeviceDisconnected();

			// TODO Release references to your characteristics.
			requiredCharacteristic = null;
			deviceNameCharacteristic = null;
			optionalCharacteristic = null;
		}

		@Override
		protected void onDeviceReady() {
			super.onDeviceReady();

			// Initialization is now ready.
			// The service or activity has been notified with TemplateManagerCallbacks#onDeviceReady().
			// TODO Do some extra logic here, of remove onDeviceReady().

			// Device is ready, let's read something here. Usually there is nothing else to be done
			// here, as all had been done during initialization.
			readCharacteristic(optionalCharacteristic)
					.with((device, data) -> {
						// Characteristic value has been read
						// Let's do some magic with it.
						if (data.size() > 0) {
							final Integer value = data.getIntValue(Data.FORMAT_UINT8, 0);
							log(LogContract.Log.Level.APPLICATION, "Value '" + value + "' has been read!");
						} else {
							log(Log.WARN, "Value is empty!");
						}
					})
					.enqueue();
		}
	}

	// TODO Define manager's API

	/**
	 * This method will write important data to the device.
	 *
	 * @param parameter parameter to be written.
	 */
	void performAction(final String parameter) {
		log(Log.VERBOSE, "Changing device name to \"" + parameter + "\"");
		// Write some data to the characteristic.
		writeCharacteristic(deviceNameCharacteristic, Data.from(parameter))
				// If data are longer than MTU-3, they will be chunked into multiple packets.
				// Check out other split options, with .split(...).
				.split()
				// Callback called when data were sent, or added to outgoing queue in case
				// Write Without Request type was used.
				.with((device, data) -> log(Log.DEBUG, data.size() + " bytes were sent"))
				// Callback called when data were sent, or added to outgoing queue in case
				// Write Without Request type was used. This is called after .with(...) callback.
				.done(device -> log(LogContract.Log.Level.APPLICATION, "Device name set to \"" + parameter + "\""))
				// Callback called when write has failed.
				.fail((device, status) -> log(Log.WARN, "Failed to change device name"))
				.enqueue();
	}
}
