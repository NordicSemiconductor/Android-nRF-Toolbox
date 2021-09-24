package no.nordicsemi.android.service

import no.nordicsemi.android.ble.BleManagerCallbacks
import no.nordicsemi.android.ble.common.profile.battery.BatteryLevelCallback

interface BatteryManagerCallbacks : BleManagerCallbacks, BatteryLevelCallback
