package no.nordicsemi.android.cgms.data

import android.bluetooth.BluetoothDevice
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.cgms.repository.CGMManager
import no.nordicsemi.android.cgms.repository.CGMService
import no.nordicsemi.android.service.BleManagerResult
import no.nordicsemi.android.service.ConnectingResult
import no.nordicsemi.android.service.ServiceManager
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CGMRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
) {
    private var manager: CGMManager? = null

    private val _data = MutableStateFlow<BleManagerResult<CGMData>>(ConnectingResult())
    internal val data = _data.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    fun launch(device: BluetoothDevice) {
        serviceManager.startService(CGMService::class.java, device)
    }

    fun start(device: BluetoothDevice, scope: CoroutineScope) {
        val manager = CGMManager(context, scope)
        this.manager = manager

        manager.dataHolder.status.onEach {
            _data.value = it
        }.launchIn(scope)

        scope.launch {
            manager.start(device)
        }
    }

    private suspend fun CGMManager.start(device: BluetoothDevice) {
        try {
            connect(device)
                .useAutoConnect(false)
                .retry(3, 100)
                .suspend()
            _isRunning.value = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun requestAllRecords() {
        manager?.requestAllRecords()
    }

    fun requestLastRecord() {
        manager?.requestLastRecord()
    }

    fun requestFirstRecord() {
        manager?.requestFirstRecord()
    }

    fun release() {
        serviceManager.stopService(CGMService::class.java)
        manager?.disconnect()?.enqueue()
        manager = null
        _isRunning.value = false
    }
}
