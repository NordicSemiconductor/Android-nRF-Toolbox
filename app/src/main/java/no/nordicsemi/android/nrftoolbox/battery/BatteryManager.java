package no.nordicsemi.android.nrftoolbox.battery;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelDataCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.nrftoolbox.profile.LoggableBleManager;

/**
 * The Ble Manager with Battery Service support.
 *
 * @param <T> The profile callbacks type.
 * @see BleManager
 */
public abstract class BatteryManager<T extends BatteryManagerCallbacks> extends LoggableBleManager<T> {
	/** Battery Service UUID. */
	private final static UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
	/** Battery Level characteristic UUID. */
	private final static UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

	private BluetoothGattCharacteristic batteryLevelCharacteristic;
	/** Last received Battery Level value. */
	private Integer batteryLevel;

	/**
	 * The manager constructor.
	 *
	 * @param context context.
	 */
	public BatteryManager(final Context context) {
		super(context);
	}

	private DataReceivedCallback batteryLevelDataCallback = new BatteryLevelDataCallback() {
		@Override
		public void onBatteryLevelChanged(@NonNull final BluetoothDevice device,
										  @IntRange(from = 0, to = 100) final int batteryLevel) {
			log(LogContract.Log.Level.APPLICATION,"Battery Level received: " + batteryLevel + "%");
			BatteryManager.this.batteryLevel = batteryLevel;
			mCallbacks.onBatteryLevelChanged(device, batteryLevel);
		}

		@Override
		public void onInvalidDataReceived(@NonNull final BluetoothDevice device, final @NonNull Data data) {
			log(Log.WARN, "Invalid Battery Level data received: " + data);
		}
	};

	public void readBatteryLevelCharacteristic() {
		if (isConnected()) {
			readCharacteristic(batteryLevelCharacteristic)
					.with(batteryLevelDataCallback)
					.fail((device, status) -> log(Log.WARN,"Battery Level characteristic not found"))
					.enqueue();
		}
	}

	public void enableBatteryLevelCharacteristicNotifications() {
		if (isConnected()) {
			// If the Battery Level characteristic is null, the request will be ignored
			setNotificationCallback(batteryLevelCharacteristic)
					.with(batteryLevelDataCallback);
			enableNotifications(batteryLevelCharacteristic)
					.done(device -> log(Log.INFO, "Battery Level notifications enabled"))
					.fail((device, status) -> log(Log.WARN, "Battery Level characteristic not found"))
					.enqueue();
		}
	}

	/**
	 * Disables Battery Level notifications on the Server.
	 */
	public void disableBatteryLevelCharacteristicNotifications() {
		if (isConnected()) {
			disableNotifications(batteryLevelCharacteristic)
					.done(device -> log(Log.INFO, "Battery Level notifications disabled"))
					.enqueue();
		}
	}

	/**
	 * Returns the last received Battery Level value.
	 * The value is set to null when the device disconnects.
	 * @return Battery Level value, in percent.
	 */
	public Integer getBatteryLevel() {
		return batteryLevel;
	}

	protected abstract class BatteryManagerGattCallback extends BleManagerGattCallback {

		@Override
		protected void initialize() {
			readBatteryLevelCharacteristic();
			enableBatteryLevelCharacteristicNotifications();
		}

		@Override
		protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(BATTERY_SERVICE_UUID);
			if (service != null) {
				batteryLevelCharacteristic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID);
			}
			return batteryLevelCharacteristic != null;
		}

		@Override
		protected void onDeviceDisconnected() {
			batteryLevelCharacteristic = null;
			batteryLevel = null;
		}
	}
}
