package no.nordicsemi.android.rscs.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.logger.ToolboxLogger
import no.nordicsemi.android.rscs.data.RSCSData
import no.nordicsemi.android.rscs.data.RSCSManager
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.service.ConnectingResult
import no.nordicsemi.android.service.ServiceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RSCSRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager
) {
    private var manager: RSCSManager? = null

    private val _data = MutableStateFlow<BleManagerResult<RSCSData>>(ConnectingResult())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.isRunning() }
    val hasBeenDisconnected = data.map { it.hasBeenDisconnected() }

    fun launch(device: BluetoothDevice) {
        serviceManager.startService(RSCSService::class.java, device)
    }

    fun start(device: BluetoothDevice, scope: CoroutineScope) {
        val manager = RSCSManager(context, scope, ToolboxLogger(context, "RSCS"))
        this.manager = manager

        manager.dataHolder.status.onEach {
            _data.value = it
        }.launchIn(scope)

        scope.launch {
            manager.start(device)
        }
    }

    private suspend fun RSCSManager.start(device: BluetoothDevice) {
        try {
            connect(device)
                .useAutoConnect(false)
                .retry(3, 100)
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        manager?.disconnect()?.enqueue()
        manager = null
    }
}
