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

package no.nordicsemi.android.nrftoolbox.uart;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import java.util.UUID;

import no.nordicsemi.android.ble.WriteRequest;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.profile.LoggableBleManager;

public class UARTManager extends LoggableBleManager<UARTManagerCallbacks> {
	/** Nordic UART Service UUID */
	private final static UUID UART_SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
	/** RX characteristic UUID */
	private final static UUID UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
	/** TX characteristic UUID */
	private final static UUID UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

	private BluetoothGattCharacteristic rxCharacteristic, txCharacteristic;
	/**
	 * A flag indicating whether Long Write can be used. It's set to false if the UART RX
	 * characteristic has only PROPERTY_WRITE_NO_RESPONSE property and no PROPERTY_WRITE.
	 * If you set it to false here, it will never use Long Write.
	 *
	 * TODO change this flag if you don't want to use Long Write even with Write Request.
	 */
	private boolean useLongWrite = true;

	UARTManager(final Context context) {
		super(context);
	}

	@NonNull
	@Override
	protected BleManagerGattCallback getGattCallback() {
		return new UARTManagerGattCallback();
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery,
	 * receiving indication, etc.
	 */
	private class UARTManagerGattCallback extends BleManagerGattCallback {

		@Override
		protected void initialize() {
			setNotificationCallback(txCharacteristic)
					.with((device, data) -> {
						final String text = data.getStringValue(0);
						log(LogContract.Log.Level.APPLICATION, "\"" + text + "\" received");
						mCallbacks.onDataReceived(device, text);
					});
			requestMtu(260).enqueue();
			enableNotifications(txCharacteristic).enqueue();
		}

		@Override
		public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(UART_SERVICE_UUID);
			if (service != null) {
				rxCharacteristic = service.getCharacteristic(UART_RX_CHARACTERISTIC_UUID);
				txCharacteristic = service.getCharacteristic(UART_TX_CHARACTERISTIC_UUID);
			}

			boolean writeRequest = false;
			boolean writeCommand = false;
			if (rxCharacteristic != null) {
				final int rxProperties = rxCharacteristic.getProperties();
				writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
				writeCommand = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0;

				// Set the WRITE REQUEST type when the characteristic supports it.
				// This will allow to send long write (also if the characteristic support it).
				// In case there is no WRITE REQUEST property, this manager will divide texts
				// longer then MTU-3 bytes into up to MTU-3 bytes chunks.
				if (writeRequest)
					rxCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
				else
					useLongWrite = false;
			}

			return rxCharacteristic != null && txCharacteristic != null && (writeRequest || writeCommand);
		}

		@Override
		protected void onDeviceDisconnected() {
			rxCharacteristic = null;
			txCharacteristic = null;
			useLongWrite = true;
		}
	}

	// This has been moved to the service in BleManager v2.0.
	/*@Override
	protected boolean shouldAutoConnect() {
		// We want the connection to be kept
		return true;
	}*/

	/**
	 * Sends the given text to RX characteristic.
	 * @param text the text to be sent
	 */
	public void send(final String text) {
		// Are we connected?
		if (rxCharacteristic == null)
			return;

		if (!TextUtils.isEmpty(text)) {
			final WriteRequest request = writeCharacteristic(rxCharacteristic, text.getBytes())
					.with((device, data) -> log(LogContract.Log.Level.APPLICATION,
							"\"" + data.getStringValue(0) + "\" sent"));
			if (!useLongWrite) {
				// This will automatically split the long data into MTU-3-byte long packets.
				request.split();
			}
			request.enqueue();
		}
	}
}
