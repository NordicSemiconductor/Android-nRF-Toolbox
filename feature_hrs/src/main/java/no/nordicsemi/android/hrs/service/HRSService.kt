package no.nordicsemi.android.hrs.service

import android.bluetooth.BluetoothDevice
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.ble.BleManagerCallbacks
import no.nordicsemi.android.hrs.data.HRSData
import no.nordicsemi.android.service.ForegroundBleService
import no.nordicsemi.android.service.LoggableBleManager
import javax.inject.Inject

@AndroidEntryPoint
internal class HRSService : ForegroundBleService<HRSManager>(), HRSManagerCallbacks {

    private var data = HRSData()
    private val points = mutableListOf<Int>()

    @Inject
    lateinit var localBroadcast: HRSDataBroadcast

    override val manager: HRSManager by lazy {
        HRSManager(this).apply {
            setGattCallbacks(this@HRSService)
        }
    }

    override fun initializeManager(): LoggableBleManager<out BleManagerCallbacks> {
        return manager
    }

    override fun onBatteryLevelChanged(device: BluetoothDevice, batteryLevel: Int) {
        sendNewData(data.copy(batteryLevel = batteryLevel))
    }

    override fun onBodySensorLocationReceived(device: BluetoothDevice, sensorLocation: Int) {
        sendNewData(data.copy(sensorLocation = sensorLocation))
    }

    override fun onHeartRateMeasurementReceived(
        device: BluetoothDevice,
        heartRate: Int,
        contactDetected: Boolean?,
        energyExpanded: Int?,
        rrIntervals: MutableList<Int>?
    ) {
        points.add(heartRate)
        sendNewData(data.copy(heartRates = points))
    }

    private fun sendNewData(newData: HRSData) {
        data = newData
        localBroadcast.offer(newData)
    }
}
