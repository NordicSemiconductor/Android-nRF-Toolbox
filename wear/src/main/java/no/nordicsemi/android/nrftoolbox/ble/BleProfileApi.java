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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;

public interface BleProfileApi {

	/**
	 * On Android, when multiple BLE operations needs to be done, it is required to wait for a proper
	 * {@link android.bluetooth.BluetoothGattCallback BluetoothGattCallback} callback before calling
	 * another operation. In order to make BLE operations easier the BleManager allows to enqueue a request
	 * containing all data necessary for a given operation. Requests are performed one after another until the
	 * queue is empty. Use static methods from below to instantiate a request and then enqueue them using {@link #enqueue(Request)}.
	 */
	final class Request {
		enum Type {
			CREATE_BOND,
			WRITE,
			READ,
			WRITE_DESCRIPTOR,
			READ_DESCRIPTOR,
			ENABLE_NOTIFICATIONS,
			ENABLE_INDICATIONS,
			READ_BATTERY_LEVEL,
			ENABLE_BATTERY_LEVEL_NOTIFICATIONS,
			DISABLE_BATTERY_LEVEL_NOTIFICATIONS,
			ENABLE_SERVICE_CHANGED_INDICATIONS,
			REQUEST_MTU,
			REQUEST_CONNECTION_PRIORITY,
		}

		final Type type;
		final BluetoothGattCharacteristic characteristic;
		final BluetoothGattDescriptor descriptor;
		final byte[] data;
		final int writeType;
		final int value;

		private Request(final Type type) {
			this.type = type;
			this.characteristic = null;
			this.descriptor = null;
			this.data = null;
			this.writeType = 0;
			this.value = 0;
		}

		private Request(final Type type, final int value) {
			this.type = type;
			this.characteristic = null;
			this.descriptor = null;
			this.data = null;
			this.writeType = 0;
			this.value = value;
		}

		private Request(final Type type, final BluetoothGattCharacteristic characteristic) {
			this.type = type;
			this.characteristic = characteristic;
			this.descriptor = null;
			this.data = null;
			this.writeType = 0;
			this.value = 0;
		}

		private Request(final Type type, final BluetoothGattCharacteristic characteristic, final int writeType, final byte[] data, final int offset, final int length) {
			this.type = type;
			this.characteristic = characteristic;
			this.descriptor = null;
			this.data = copy(data, offset, length);
			this.writeType = writeType;
			this.value = 0;
		}

		private Request(final Type type, final BluetoothGattDescriptor descriptor) {
			this.type = type;
			this.characteristic = null;
			this.descriptor = descriptor;
			this.data = null;
			this.writeType = 0;
			this.value = 0;
		}

		private Request(final Type type, final BluetoothGattDescriptor descriptor, final byte[] data, final int offset, final int length) {
			this.type = type;
			this.characteristic = null;
			this.descriptor = descriptor;
			this.data = copy(data, offset, length);
			this.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
			this.value = 0;
		}

		private static byte[] copy(final byte[] value, final int offset, final int length) {
			if (value == null || offset > value.length)
				return null;
			final int maxLength = Math.min(value.length - offset, length);
			final byte[] copy = new byte[maxLength];
			System.arraycopy(value, offset, copy, 0, maxLength);
			return copy;
		}

		/**
		 * Creates a new request that will start pairing with the device.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request createBond() {
			return new Request(Type.CREATE_BOND);
		}

		/**
		 * Creates new Read Characteristic request. The request will not be executed if given characteristic
		 * is null or does not have READ property. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to be read
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newReadRequest(final BluetoothGattCharacteristic characteristic) {
			return new Request(Type.READ, characteristic);
		}

		/**
		 * Creates new Write Characteristic request. The request will not be executed if given characteristic
		 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to be written
		 * @param data data to be written. The array is copied into another buffer so it's safe to reuse the array again.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] data) {
			return new Request(Type.WRITE, characteristic, characteristic.getWriteType(), data, 0, data != null ? data.length : 0);
		}

		/**
		 * Creates new Write Characteristic request. The request will not be executed if given characteristic
		 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to be written
		 * @param data data to be written. The array is copied into another buffer so it's safe to reuse the array again.
		 * @param writeType write type to be used, one of {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT}, {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE}.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] data, final int writeType) {
			return new Request(Type.WRITE, characteristic, writeType, data, 0, data != null ? data.length : 0);
		}

		/**
		 * Creates new Write Characteristic request. The request will not be executed if given characteristic
		 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to be written
		 * @param data data to be written. The array is copied into another buffer so it's safe to reuse the array again.
		 * @param offset the offset from which data has to be copied
		 * @param length number of bytes to be copied from the data buffer
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] data, final int offset, final int length) {
			return new Request(Type.WRITE, characteristic, characteristic.getWriteType(), data, offset, length);
		}

		/**
		 * Creates new Write Characteristic request. The request will not be executed if given characteristic
		 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to be written
		 * @param data data to be written. The array is copied into another buffer so it's safe to reuse the array again.
		 * @param offset the offset from which data has to be copied
		 * @param length number of bytes to be copied from the data buffer
		 * @param writeType write type to be used, one of {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT}, {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE}.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] data, final int offset, final int length, final int writeType) {
			return new Request(Type.WRITE, characteristic, writeType, data, offset, length);
		}

		/**
		 * Creates new Read Descriptor request. The request will not be executed if given descriptor
		 * is null. After the operation is complete a proper callback will be invoked.
		 * @param descriptor descriptor to be read
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newReadRequest(final BluetoothGattDescriptor descriptor) {
			return new Request(Type.READ_DESCRIPTOR, descriptor);
		}

		/**
		 * Creates new Write Descriptor request. The request will not be executed if given descriptor
		 * is null. After the operation is complete a proper callback will be invoked.
		 * @param descriptor descriptor to be written
		 * @param data data to be written. The array is copied into another buffer so it's safe to reuse the array again.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newWriteRequest(final BluetoothGattDescriptor descriptor, final byte[] data) {
			return new Request(Type.WRITE_DESCRIPTOR, descriptor, data, 0, data != null ? data.length : 0);
		}

		/**
		 * Creates new Write Descriptor request. The request will not be executed if given descriptor
		 * is null. After the operation is complete a proper callback will be invoked.
		 * @param descriptor descriptor to be written
		 * @param data data to be written. The array is copied into another buffer so it's safe to reuse the array again.
		 * @param offset the offset from which data has to be copied
		 * @param length number of bytes to be copied from the data buffer
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newWriteRequest(final BluetoothGattDescriptor descriptor, final byte[] data, final int offset, final int length) {
			return new Request(Type.WRITE_DESCRIPTOR, descriptor, data, offset, length);
		}

		/**
		 * Creates new Enable Notification request. The request will not be executed if given characteristic
		 * is null, does not have NOTIFY property or the CCCD. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to have notifications enabled
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newEnableNotificationsRequest(final BluetoothGattCharacteristic characteristic) {
			return new Request(Type.ENABLE_NOTIFICATIONS, characteristic);
		}

		/**
		 * Creates new Enable Indications request. The request will not be executed if given characteristic
		 * is null, does not have INDICATE property or the CCCD. After the operation is complete a proper callback will be invoked.
		 * @param characteristic characteristic to have indications enabled
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newEnableIndicationsRequest(final BluetoothGattCharacteristic characteristic) {
			return new Request(Type.ENABLE_INDICATIONS, characteristic);
		}

		/**
		 * Reads the first found Battery Level characteristic value from the first found Battery Service.
		 * If any of them is not found, or the characteristic does not have the READ property this operation will not execute.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newReadBatteryLevelRequest() {
			return new Request(Type.READ_BATTERY_LEVEL); // the first Battery Level char from the first Battery Service is used
		}

		/**
		 * Enables notifications on the first found Battery Level characteristic from the first found Battery Service.
		 * If any of them is not found, or the characteristic does not have the NOTIFY property this operation will not execute.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newEnableBatteryLevelNotificationsRequest() {
			return new Request(Type.ENABLE_BATTERY_LEVEL_NOTIFICATIONS); // the first Battery Level char from the first Battery Service is used
		}

		/**
		 * Disables notifications on the first found Battery Level characteristic from the first found Battery Service.
		 * If any of them is not found, or the characteristic does not have the NOTIFY property this operation will not execute.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		public static Request newDisableBatteryLevelNotificationsRequest() {
			return new Request(Type.DISABLE_BATTERY_LEVEL_NOTIFICATIONS); // the first Battery Level char from the first Battery Service is used
		}

		/**
		 * Enables indications on Service Changed characteristic if such exists in the Generic Attribute service.
		 * It is required to enable those notifications on bonded devices on older Android versions to be
		 * informed about attributes changes. Android 7+ (or 6+) handles this automatically and no action is required.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		static Request newEnableServiceChangedIndicationsRequest() {
			return new Request(Type.ENABLE_SERVICE_CHANGED_INDICATIONS); // the only Service Changed char is used (if such exists)
		}

		/**
		 * Requests new MTU (Maximum Transfer Unit). This is only supported on Android Lollipop or newer.
		 * The target device may reject requested data and set smalled MTU.
		 * @param mtu the new MTU. Acceptable values are &lt;23, 517&gt;.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		static Request newMtuRequest(int mtu) {
			if (mtu < 23)
				mtu = 23;
			if (mtu > 517)
				mtu = 517;
			return new Request(Type.REQUEST_MTU, mtu);
		}

		/**
		 * Requests the new connection priority. Acceptable values are:
		 * <ol>
		 *     <li>{@link BluetoothGatt#CONNECTION_PRIORITY_HIGH} - Interval: 11.25 -15 ms, latency: 0, supervision timeout: 20 sec,</li>
		 *     <li>{@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED} - Interval: 30 - 50 ms, latency: 0, supervision timeout: 20 sec,</li>
		 *     <li>{@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER} - Interval: 100 - 125 ms, latency: 2, supervision timeout: 20 sec.</li>
		 * </ol>
		 *
		 * @param priority one of: {@link BluetoothGatt#CONNECTION_PRIORITY_HIGH}, {@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED},
		 *                 {@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER}.
		 * @return the new request that can be enqueued using {@link #enqueue(Request)} method.
		 */
		static Request newConnectionPriorityRequest(int priority) {
			if (priority < 0 || priority > 2)
				priority = 0; // Balanced
			return new Request(Type.REQUEST_CONNECTION_PRIORITY, priority);
		}
	}

	/**
	 * Returns the context.
	 */
	Context getContext();

	/**
	 * Enqueues creating bond request to the queue.
	 * @return true if request has been enqueued, false if the device has not been connected
	 */
	boolean createBond();

	/**
	 * Enables notifications on given characteristic
	 *
	 * @return true is the request has been enqueued
	 */
	boolean enableNotifications(final BluetoothGattCharacteristic characteristic);

	/**
	 * Enables indications on given characteristic
	 *
	 * @return true is the request has been enqueued
	 */
	boolean enableIndications(final BluetoothGattCharacteristic characteristic);

	/**
	 * Sends the read request to the given characteristic.
	 *
	 * @param characteristic the characteristic to read
	 * @return true if request has been enqueued
	 */
	boolean readCharacteristic(final BluetoothGattCharacteristic characteristic);

	/**
	 * Writes the characteristic value to the given characteristic.
	 *
	 * @param characteristic the characteristic to write to
	 * @return true if request has been enqueued
	 */
	boolean writeCharacteristic(final BluetoothGattCharacteristic characteristic);

	/**
	 * Sends the read request to the given descriptor.
	 *
	 * @param descriptor the descriptor to read
	 * @return true if request has been enqueued
	 */
	boolean readDescriptor(final BluetoothGattDescriptor descriptor);

	/**
	 * Writes the descriptor value to the given descriptor.
	 *
	 * @param descriptor the descriptor to write to
	 * @return true if request has been enqueued
	 */
	boolean writeDescriptor(final BluetoothGattDescriptor descriptor);

	/**
	 * Reads the battery level from the device.
	 *
	 * @return true if request has been enqueued
	 */
	boolean readBatteryLevel();

	/**
	 * This method tries to enable notifications on the Battery Level characteristic.
	 *
	 * @param enable <code>true</code> to enable battery notifications, false to disable
	 * @return true if request has been enqueued
	 */
	boolean setBatteryNotifications(final boolean enable);

	/**
	 * Requests new MTU. On Android 4.3 and 4.4.x returns false.
	 *
	 * @return true if request has been enqueued
	 */
	boolean requestMtu(final int mtu);

	/**
	 * Returns the current MTU (Maximum Transfer Unit). MTU specifies the maximum number of bytes that can
	 * be sent in a single write operation. 3 bytes are used for internal purposes, so the maximum size is MTU-3.
	 * The value will changed only if requested with {@link #requestMtu(int)} and a successful callback is received.
	 * If the peripheral requests MTU change, the {@link BluetoothGattCallback#onMtuChanged(BluetoothGatt, int, int)}
	 * callback is not invoked, therefor the returned MTU value will not be correct.
	 * Use {@link android.bluetooth.BluetoothGattServerCallback#onMtuChanged(BluetoothDevice, int)} to get the
	 * callback with right value requested from the peripheral side.
	 * @return the current MTU value. Default to 23.
	 */
	int getMtu();

	/**
	 * This method overrides the MTU value. Use it only when the peripheral has changed MTU and you
	 * received the {@link android.bluetooth.BluetoothGattServerCallback#onMtuChanged(BluetoothDevice, int)}
	 * callback. If you want to set MTU as a master, use {@link #requestMtu(int)} instead.
	 * @param mtu the MTU value set by the peripheral.
	 */
	void overrideMtu(final int mtu);

	/**
	 * Requests the new connection priority. Acceptable values are:
	 * <ol>
	 *     <li>{@link BluetoothGatt#CONNECTION_PRIORITY_HIGH} - Interval: 11.25 -15 ms, latency: 0, supervision timeout: 20 sec,</li>
	 *     <li>{@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED} - Interval: 30 - 50 ms, latency: 0, supervision timeout: 20 sec,</li>
	 *     <li>{@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER} - Interval: 100 - 125 ms, latency: 2, supervision timeout: 20 sec.</li>
	 * </ol>
	 * On Android 4.3 and 4.4.x returns false.
	 *
	 * @param priority one of: {@link BluetoothGatt#CONNECTION_PRIORITY_HIGH}, {@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED},
	 *                 {@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER}.
	 * @return true if request has been enqueued
	 */
	boolean requestConnectionPriority(final int priority);

	/**
	 * Enqueues a new request. The request will be handled immediately if there is no operation in progress,
	 * or automatically after the last enqueued one will finish.
	 * <p>This method should be used to read and write data from the target device as it ensures that the last operation has finished
	 * before a new one will be called.</p>
	 * @param request new request to be performed
	 * @return true if request has been enqueued, false if the device is not connected
	 */
	boolean enqueue(final Request request);
}
