package no.nordicsemi.android.hts.service

import android.bluetooth.BluetoothDevice
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.ble.BleManagerCallbacks
import no.nordicsemi.android.hts.data.HTSData
import no.nordicsemi.android.service.ForegroundBleService
import no.nordicsemi.android.service.LoggableBleManager
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
internal class HTSService : ForegroundBleService<HTSManager>(), HTSManagerCallbacks {

    private var data = HTSData()
    private val points = mutableListOf<Int>()

    @Inject
    lateinit var localBroadcast: HTSDataBroadcast

    override val manager: HTSManager by lazy {
        HTSManager(this).apply {
            setGattCallbacks(this@HTSService)
        }
    }

    override fun initializeManager(): LoggableBleManager<out BleManagerCallbacks> {
        return manager
    }

    override fun onBatteryLevelChanged(device: BluetoothDevice, batteryLevel: Int) {
        sendNewData(data.copy(batteryLevel = batteryLevel))
    }

    override fun onTemperatureMeasurementReceived(
        device: BluetoothDevice,
        temperature: Float,
        unit: Int,
        calendar: Calendar?,
        type: Int?
    ) {
        TODO("Not yet implemented")
    }

    private fun sendNewData(newData: HTSData) {
        data = newData
        localBroadcast.offer(newData)
    }
}
