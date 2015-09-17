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
package no.nordicsemi.android.nrftoolbox.profile;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;

public interface BleManagerCallbacks {

	/**
	 * Called when the device has been connected. This does not mean that the application may start communication. A service discovery will be handled automatically after this call. Service discovery
	 * may ends up with calling {@link #onServicesDiscovered(boolean)} or {@link #onDeviceNotSupported()} if required services have not been found.
	 */
	public void onDeviceConnected();

	/**
	 * Called when user initialized disconnection.
	 */
	public void onDeviceDisconnecting();

	/**
	 * Called when the device has disconnected (when the callback returned {@link BluetoothGattCallback#onConnectionStateChange(BluetoothGatt, int, int)} with state DISCONNECTED.
	 */
	public void onDeviceDisconnected();

	/**
	 * This callback is invoked when the Ble Manager lost connection to a device that has been connected with autoConnect option. Otherwise a {@link #onDeviceDisconnected()}
	 * method will be called on such event.
	 */
	public void onLinklossOccur();

	/**
	 * Called when service discovery has finished and primary services has been found. The device is ready to operate. This method is not called if the primary, mandatory services were not found
	 * during service discovery. For example in the Blood Pressure Monitor, a Blood Pressure service is a primary service and Intermediate Cuff Pressure service is a optional secondary service.
	 * Existence of battery service is not notified by this call.
	 * 
	 * @param optionalServicesFound
	 *            if <code>true</code> the secondary services were also found on the device.
	 */
	public void onServicesDiscovered(final boolean optionalServicesFound);

	/**
	 * Method called when all initialization requests has been completed.
	 */
	public void onDeviceReady();

	/**
	 * Called when battery value has been received from the device.
	 * 
	 * @param value
	 *            the battery value in percent
	 */
	public void onBatteryValueReceived(final int value);

	/**
	 * Called when an {@link BluetoothGatt#GATT_INSUFFICIENT_AUTHENTICATION} error occurred and the device bond state is NOT_BONDED
	 */
	public void onBondingRequired();

	/**
	 * Called when the device has been successfully bonded.
	 */
	public void onBonded();

	/**
	 * Called when a BLE error has occurred
	 * 
	 * @param message
	 *            the error message
	 * @param errorCode
	 *            the error code
	 */
	public void onError(final String message, final int errorCode);

	/**
	 * Called when service discovery has finished but the main services were not found on the device.
	 */
	public void onDeviceNotSupported();
}
