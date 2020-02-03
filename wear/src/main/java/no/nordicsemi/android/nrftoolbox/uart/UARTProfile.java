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
import android.text.TextUtils;

import java.util.Deque;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.ble.BleManager;
import no.nordicsemi.android.nrftoolbox.ble.BleProfile;
import no.nordicsemi.android.nrftoolbox.ble.BleProfileApi;

public class UARTProfile extends BleProfile {
	/** Broadcast sent when a UART message is received. */
	static final String BROADCAST_DATA_RECEIVED = "no.nordicsemi.android.nrftoolbox.uart.BROADCAST_DATA_RECEIVED";
	/** The message. */
	@SuppressWarnings("unused")
	public static final String EXTRA_DATA = "no.nordicsemi.android.nrftoolbox.EXTRA_DATA";

	/** Nordic UART Service UUID */
	private static final UUID UART_SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
	/** RX characteristic UUID */
	private static final UUID UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
	/** TX characteristic UUID */
	private static final UUID UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
	/** The maximum packet size is 20 bytes. */
	private static final int MAX_PACKET_SIZE = 20;

	/**
	 * This method should return true if the profile matches the given device. That means if the device has the required services.
	 * @param gatt the GATT device
	 * @return true if the device is supported by that profile, false otherwise.
	 */
	public static boolean matchDevice(final BluetoothGatt gatt) {
		final BluetoothGattService service = gatt.getService(UART_SERVICE_UUID);
		return service != null
				&& service.getCharacteristic(UART_TX_CHARACTERISTIC_UUID) != null
				&& service.getCharacteristic(UART_RX_CHARACTERISTIC_UUID) != null;
	}

	@SuppressWarnings("unused")
	private BluetoothGattCharacteristic txCharacteristic;
	private BluetoothGattCharacteristic rxCharacteristic;
	private byte[] outgoingBuffer;
	private int bufferOffset;

	@Override
	protected Deque<BleManager.Request> initGatt(final BluetoothGatt gatt) {
		final BluetoothGattService service = gatt.getService(UART_SERVICE_UUID);
		txCharacteristic = service.getCharacteristic(UART_TX_CHARACTERISTIC_UUID);
		rxCharacteristic = service.getCharacteristic(UART_RX_CHARACTERISTIC_UUID);

		final int rxProperties = rxCharacteristic.getProperties();
		boolean writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;

		// Set the WRITE REQUEST type when the characteristic supports it. This will allow to send
		// long write (also if the characteristic support it).
		// In case there is no WRITE REQUEST property, this manager will divide texts longer then
		// 20 bytes into up to 20 bytes chunks.
		if (writeRequest)
			rxCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

		// We don't want to enable notifications on TX characteristic as we are not showing them here.
		// A watch may be just used to send data. At least now.
//		final LinkedList<BleProfileApi.Request> requests = new LinkedList<>();
//		requests.add(BleProfileApi.Request.newEnableNotificationsRequest(txCharacteristic));
//		return requests;
		return null;
	}

	@Override
	protected void release() {
		txCharacteristic = null;
		rxCharacteristic = null;
	}

	@Override
	protected void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
		// This method will not be called as notifications were not enabled in initGatt(..).
//		final Intent intent = new Intent(BROADCAST_DATA_RECEIVED);
//		intent.putExtra(EXTRA_DATA, characteristic.getStringValue(0));
//		LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
	}

	@Override
	protected void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
		// When the whole buffer has been sent
		final byte[] buffer = outgoingBuffer;
		if (bufferOffset == buffer.length) {
			outgoingBuffer = null;
		} else { // Otherwise...
			final int length = Math.min(buffer.length - bufferOffset, MAX_PACKET_SIZE);
			getApi().enqueue(BleProfileApi.Request.newWriteRequest(rxCharacteristic, buffer, bufferOffset, length));
			bufferOffset += length;
		}
	}

	/**
	 * Sends the given text to RX characteristic.
	 * @param text the text to be sent
	 */
	void send(final String text) {
		// Are we connected?
		if (rxCharacteristic == null)
			return;

		// An outgoing buffer may not be null if there is already another packet being sent. We do nothing in this case.
		if (!TextUtils.isEmpty(text) && outgoingBuffer == null) {
			final byte[] buffer = outgoingBuffer = text.getBytes();
			bufferOffset = 0;

			// Depending on whether the characteristic has the WRITE REQUEST property or not, we will either send it as it is (hoping the long write is implemented),
			// or divide it into up to 20 bytes chunks and send them one by one.
			final boolean writeRequest = (rxCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;

			if (!writeRequest) { // no WRITE REQUEST property
				final int length = Math.min(buffer.length, MAX_PACKET_SIZE);
				bufferOffset += length;
				getApi().enqueue(BleProfileApi.Request.newWriteRequest(rxCharacteristic, buffer, 0, length));
			} else { // there is WRITE REQUEST property, let's try Long Write
				bufferOffset = buffer.length;
				getApi().enqueue(BleProfileApi.Request.newWriteRequest(rxCharacteristic, buffer, 0, buffer.length));
			}
		}
	}
}
