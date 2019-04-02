package no.nordicsemi.android.nrftoolbox.template.callback;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * This is a sample data callback, that's based on Heart Rate Measurement characteristic.
 * It parses the HR value and ignores other optional data for simplicity.
 * Check {@link no.nordicsemi.android.ble.common.callback.hr.HeartRateMeasurementDataCallback}
 * for full implementation.
 *
 * TODO Modify the content to parse your data.
 */
@SuppressWarnings("ConstantConditions")
public abstract class TemplateDataCallback implements ProfileDataCallback, TemplateCharacteristicCallback {

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		if (data.size() < 2) {
			onInvalidDataReceived(device, data);
			return;
		}

		// Read flags
		int offset = 0;
		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset);
		final int hearRateType = (flags & 0x01) == 0 ? Data.FORMAT_UINT8 : Data.FORMAT_UINT16;
		offset += 1;

		// Validate packet length. The type's lower nibble is its length.
		if (data.size() < 1 + (hearRateType & 0x0F)) {
			onInvalidDataReceived(device, data);
			return;
		}

		final int value = data.getIntValue(hearRateType, offset);
		// offset += hearRateType & 0xF;

		// ...

		// Report the parsed value(s)
		onSampleValueReceived(device, value);
	}
}
