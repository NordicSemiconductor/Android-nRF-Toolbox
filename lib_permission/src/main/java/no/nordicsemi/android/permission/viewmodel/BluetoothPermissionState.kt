package no.nordicsemi.android.permission.viewmodel

enum class BluetoothPermissionState {
    PERMISSION_REQUIRED,
    BLUETOOTH_NOT_AVAILABLE,
    BLUETOOTH_NOT_ENABLED,
    DEVICE_NOT_CONNECTED,
    BONDING_REQUIRED,
    READY
}
