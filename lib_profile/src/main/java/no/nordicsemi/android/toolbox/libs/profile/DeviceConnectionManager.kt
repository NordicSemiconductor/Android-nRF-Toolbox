package no.nordicsemi.android.toolbox.libs.profile

import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceConnectionManager @Inject constructor(
    private val centralManager: CentralManager,
) {
    val state = centralManager.state
    /**
     * Connects to the peripheral device.
     *
     * @param peripheral The peripheral to connect to.
     * @param autoConnect If `true`, the connection will be established using the Auto Connect feature.
     */
    suspend fun connectToDevice(
        peripheral: Peripheral,
        autoConnect: Boolean = false,
    ) {
        try {
            if (!peripheral.isDisconnected) return
            centralManager.connect(
                peripheral = peripheral,
                options = if (autoConnect) {
                    CentralManager.ConnectionOptions.AutoConnect
                } else CentralManager.ConnectionOptions.Direct()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e)
        }
    }
}