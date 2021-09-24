package no.nordicsemi.android.csc.service

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.csc.events.CrankDataChanged
import no.nordicsemi.android.csc.events.OnBatteryLevelChanged
import no.nordicsemi.android.csc.events.OnDistanceChangedEvent
import no.nordicsemi.android.service.ForegroundBleService
import no.nordicsemi.android.service.LoggableBleManager
import javax.inject.Inject

@AndroidEntryPoint
internal class CSCService : ForegroundBleService<CSCManager>(), CSCManagerCallbacks {

    @Inject
    lateinit var localBroadcast: CSCDataReadBroadcast

    override val manager: CSCManager by lazy {
        CSCManager(this).apply {
            setGattCallbacks(this@CSCService)
        }
    }

    override fun initializeManager(): LoggableBleManager<CSCManagerCallbacks> {
        return manager
    }

    override fun onCreate() {
        super.onCreate()

        localBroadcast.wheelSize.onEach {
            manager.setWheelSize(it)
        }.launchIn(lifecycleScope)
    }

    override fun onDistanceChanged(
        device: BluetoothDevice,
        totalDistance: Float,
        distance: Float,
        speed: Float
    ) {
        localBroadcast.offer(OnDistanceChangedEvent(bluetoothDevice, speed, distance, totalDistance))
    }

    override fun onCrankDataChanged(
        device: BluetoothDevice,
        crankCadence: Float,
        gearRatio: Float
    ) {
        localBroadcast.offer(CrankDataChanged(bluetoothDevice, crankCadence.toInt(), gearRatio))
    }

    override fun onBatteryLevelChanged(device: BluetoothDevice, batteryLevel: Int) {
        localBroadcast.offer(OnBatteryLevelChanged(bluetoothDevice, batteryLevel))
    }
}