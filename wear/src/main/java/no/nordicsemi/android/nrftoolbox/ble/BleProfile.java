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

package no.nordicsemi.android.nrftoolbox.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;

import java.util.Queue;

public abstract class BleProfile {
	private Context mContext;
	private BleProfileApi mApi;

	/* package */ void setApi(final BleProfileApi api) {
		this.mContext = api.getContext();
		this.mApi = api;
	}

	/**
	 * Returns the BLE API for sending data to the remote device.
	 */
	public BleProfileApi getApi() {
		return mApi;
	}

	/**
	 * Returns the service context.
	 * @return the context
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * This method should return a list of requests needed to initialize the profile.
	 * Enabling Service Change indications for bonded devices and reading the Battery Level value and enabling Battery Level notifications
	 * is handled before executing this queue. The queue should not have requests that are not available, e.g. should not
	 * read an optional service when it is not supported by the connected device.
	 * <p>This method is called when the services has been discovered and the device is supported (has required service).</p>
	 *
	 * @param gatt the gatt device with services discovered
	 * @return the queue of requests
	 */
	protected abstract Queue<BleManager.Request> initGatt(final BluetoothGatt gatt);

	/**
	 * Releases all profile resources. The device is no longer connected.
	 */
	protected abstract void release();

	/**
	 * Callback reporting the result of a characteristic read operation.
	 *
	 * @param gatt           GATT client invoked {@link BluetoothGatt#readCharacteristic}
	 * @param characteristic Characteristic that was read from the associated
	 *                       remote device.
	 */
	protected void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
		// do nothing
	}

	/**
	 * Callback indicating the result of a characteristic write operation.
	 * <p/>
	 * <p>If this callback is invoked while a reliable write transaction is
	 * in progress, the value of the characteristic represents the value
	 * reported by the remote device. An application should compare this
	 * value to the desired value to be written. If the values don't match,
	 * the application must abort the reliable write transaction.
	 *
	 * @param gatt           GATT client invoked {@link BluetoothGatt#writeCharacteristic}
	 * @param characteristic Characteristic that was written to the associated
	 *                       remote device.
	 */
	protected void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
		// do nothing
	}

	protected void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor) {
		// do nothing
	}

	protected void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
		// do nothing
	}

	protected void onCharacteristicIndicated(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
		// do nothing
	}

	/**
	 * Called when a BLE error has occurred
	 *
	 * @param message
	 *            the error message
	 * @param errorCode
	 *            the error code
	 */
	public void onError(final String message, final int errorCode) {
		// do nothing
	}
}
