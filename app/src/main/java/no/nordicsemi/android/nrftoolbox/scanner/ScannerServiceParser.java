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
package no.nordicsemi.android.nrftoolbox.scanner;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * ScannerServiceParser is responsible to parse scanning data and it check if scanned device has required service in it.
 */
public class ScannerServiceParser {
	private static final String TAG = "ScannerServiceParser";

	private static final int FLAGS_BIT = 0x01;
	private static final int SERVICES_MORE_AVAILABLE_16_BIT = 0x02;
	private static final int SERVICES_COMPLETE_LIST_16_BIT = 0x03;
	private static final int SERVICES_MORE_AVAILABLE_32_BIT = 0x04;
	private static final int SERVICES_COMPLETE_LIST_32_BIT = 0x05;
	private static final int SERVICES_MORE_AVAILABLE_128_BIT = 0x06;
	private static final int SERVICES_COMPLETE_LIST_128_BIT = 0x07;
	private static final int SHORTENED_LOCAL_NAME = 0x08;
	private static final int COMPLETE_LOCAL_NAME = 0x09;

	private static final byte LE_LIMITED_DISCOVERABLE_MODE = 0x01;
	private static final byte LE_GENERAL_DISCOVERABLE_MODE = 0x02;

	/**
	 * Checks if device is connectible (as Android cannot get this information directly we just check if it has GENERAL DISCOVERABLE or LIMITED DISCOVERABLE flag set) and has required service UUID in
	 * the advertising packet. The service UUID may be <code>null</code>.
	 * <p>
	 * For further details on parsing BLE advertisement packet data see https://developer.bluetooth.org/Pages/default.aspx Bluetooth Core Specifications Volume 3, Part C, and Section 8
	 * </p>
	 */
	public static boolean decodeDeviceAdvData(byte[] data, UUID requiredUUID, boolean discoverableRequired) {
		final String uuid = requiredUUID != null ? requiredUUID.toString() : null;
		if (data != null) {
			boolean connectible = !discoverableRequired;
			boolean valid = uuid == null;
			if (connectible && valid)
				return true;
			int fieldLength, fieldName;
			int packetLength = data.length;
			for (int index = 0; index < packetLength; index++) {
				fieldLength = data[index];
				if (fieldLength == 0) {
					return connectible && valid;
				}
				fieldName = data[++index];

				if (uuid != null) {
					if (fieldName == SERVICES_MORE_AVAILABLE_16_BIT || fieldName == SERVICES_COMPLETE_LIST_16_BIT) {
						for (int i = index + 1; i < index + fieldLength - 1; i += 2)
							valid = valid || decodeService16BitUUID(uuid, data, i, 2);
					} else if (fieldName == SERVICES_MORE_AVAILABLE_32_BIT || fieldName == SERVICES_COMPLETE_LIST_32_BIT) {
						for (int i = index + 1; i < index + fieldLength - 1; i += 4)
							valid = valid || decodeService32BitUUID(uuid, data, i, 4);
					} else if (fieldName == SERVICES_MORE_AVAILABLE_128_BIT || fieldName == SERVICES_COMPLETE_LIST_128_BIT) {
						for (int i = index + 1; i < index + fieldLength - 1; i += 16)
							valid = valid || decodeService128BitUUID(uuid, data, i, 16);
					}
				}
				if (!connectible && fieldName == FLAGS_BIT) {
					int flags = data[index + 1];
					connectible = (flags & (LE_GENERAL_DISCOVERABLE_MODE | LE_LIMITED_DISCOVERABLE_MODE)) > 0;
				}
				index += fieldLength - 1;
			}
			return connectible && valid;
		}
		return false;
	}

	/**
	 * Decodes the device name from Complete Local Name or Shortened Local Name field in Advertisement packet. Usually if should be done by {@link BluetoothDevice#getName()} method but some phones
	 * skips that, f.e. Sony Xperia Z1 (C6903) with Android 4.3 where getName() always returns <code>null</code>. In order to show the device name correctly we have to parse it manually :(
	 */
	public static String decodeDeviceName(byte[] data) {
		String name = null;
		int fieldLength, fieldName;
		int packetLength = data.length;
		for (int index = 0; index < packetLength; index++) {
			fieldLength = data[index];
			if (fieldLength == 0)
				break;
			fieldName = data[++index];

			if (fieldName == COMPLETE_LOCAL_NAME || fieldName == SHORTENED_LOCAL_NAME) {
				name = decodeLocalName(data, index + 1, fieldLength - 1);
				break;
			}
			index += fieldLength - 1;
		}
		return name;
	}

	/**
	 * Decodes the local name
	 */
	public static String decodeLocalName(final byte[] data, final int start, final int length) {
		try {
			return new String(data, start, length, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			Log.e(TAG, "Unable to convert the complete local name to UTF-8", e);
			return null;
		} catch (final IndexOutOfBoundsException e) {
			Log.e(TAG, "Error when reading complete local name", e);
			return null;
		}
	}

	/**
	 * check for required Service UUID inside device
	 */
	private static boolean decodeService16BitUUID(String uuid, byte[] data, int startPosition, int serviceDataLength) {
		String serviceUUID = Integer.toHexString(decodeUuid16(data, startPosition));
		String requiredUUID = uuid.substring(4, 8);

		return serviceUUID.equals(requiredUUID);
	}

	/**
	 * check for required Service UUID inside device
	 */
	private static boolean decodeService32BitUUID(String uuid, byte[] data, int startPosition, int serviceDataLength) {
		String serviceUUID = Integer.toHexString(decodeUuid16(data, startPosition + serviceDataLength - 4));
		String requiredUUID = uuid.substring(4, 8);

		return serviceUUID.equals(requiredUUID);
	}

	/**
	 * check for required Service UUID inside device
	 */
	private static boolean decodeService128BitUUID(String uuid, byte[] data, int startPosition, int serviceDataLength) {
		String serviceUUID = Integer.toHexString(decodeUuid16(data, startPosition + serviceDataLength - 4));
		String requiredUUID = uuid.substring(4, 8);

		return serviceUUID.equals(requiredUUID);
	}

	private static int decodeUuid16(final byte[] data, final int start) {
		final int b1 = data[start] & 0xff;
		final int b2 = data[start + 1] & 0xff;

		return (b2 << 8 | b1);
	}
}
