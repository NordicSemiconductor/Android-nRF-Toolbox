package no.nordicsemi.android.nrftoolbox.battery;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.common.callback.BatteryLevelDataCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;

/**
 * The Ble Manager with Battery Service support.
 * @param <T> The profile callbacks type
 * @see BleManager
 */
@SuppressWarnings("WeakerAccess")
public abstract class BatteryManager<T extends BatteryManagerCallbacks> extends BleManager<T> {
	/** Battery Service UUID. */
	private final static UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
	/** Battery Level characteristic UUID. */
	private final static UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic mBatteryLevelCharacteristic;

	/**
	 * The manager constructor.
	 *
	 * @param context context
	 */
	public BatteryManager(final Context context) {
		super(context);
	}

	@Override
	protected abstract BatteryManagerGattCallback getGattCallback();

	public void readBatteryLevelCharacteristic() {
		readCharacteristic(mBatteryLevelCharacteristic)
				.with(new BatteryLevelDataCallback() {
					@Override
					public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
						mCallbacks.onBatteryLevelChanged(device, batteryLevel);
					}

					@Override
					public void onInvalidDataReceived(@NonNull final BluetoothDevice device, final @NonNull Data data) {
						log(LogContract.Log.Level.WARNING, "Invalid Battery Level data received: " + data);
					}
				})
				.fail(status -> log(LogContract.Log.Level.WARNING, "Battery Level characteristic not found"));
	}

	public void enableBatteryLevelCharacteristicNotifications() {
		// If the Battery Level characteristic is null, the request will be ignored
		enableNotifications(mBatteryLevelCharacteristic)
				.with(new BatteryLevelDataCallback() {
					@Override
					public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
						mCallbacks.onBatteryLevelChanged(device, batteryLevel);
					}

					@Override
					public void onInvalidDataReceived(@NonNull final BluetoothDevice device, final @NonNull Data data) {
						log(LogContract.Log.Level.WARNING, "Invalid Battery Level data received: " + data);
					}
				})
				.done(() -> log(LogContract.Log.Level.INFO, "Battery Level notifications enabled"))
				.fail(status -> log(LogContract.Log.Level.WARNING, "Battery Level characteristic not found"));
	}

	public void disableBatteryLevelCharacteristicNotifications() {
		disableNotifications(mBatteryLevelCharacteristic)
				.done(() -> log(LogContract.Log.Level.INFO, "Battery Level notifications disabled"));
	}

	protected abstract class BatteryManagerGattCallback extends BleManagerGattCallback {

		@Override
		protected void initialize(@NonNull final BluetoothDevice device) {
			readBatteryLevelCharacteristic();
			enableBatteryLevelCharacteristicNotifications();
		}

		@Override
		protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(BATTERY_SERVICE_UUID);
			if (service != null) {
				mBatteryLevelCharacteristic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID);
			}
			return mBatteryLevelCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			mBatteryLevelCharacteristic = null;
		}
	}
}
