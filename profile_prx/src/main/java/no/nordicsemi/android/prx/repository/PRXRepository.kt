package no.nordicsemi.android.prx.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.logger.ToolboxLogger
import no.nordicsemi.android.prx.data.AlarmLevel
import no.nordicsemi.android.prx.data.PRXData
import no.nordicsemi.android.prx.data.PRXManager
import no.nordicsemi.android.prx.data.ProximityServerManager
import no.nordicsemi.android.service.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PRXRepository @Inject internal constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
    private val proximityServerManager: ProximityServerManager,
    private val alarmHandler: AlarmHandler
) {

    private var manager: PRXManager? = null

    private val _data = MutableStateFlow<BleManagerResult<PRXData>>(ConnectingResult())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.isRunning() }
    val hasBeenDisconnectedWithoutLinkLoss = data.map { it.hasBeenDisconnectedWithoutLinkLoss() }

    fun launch(device: BluetoothDevice) {
        serviceManager.startService(PRXService::class.java, device)
        proximityServerManager.open()
    }

    fun start(device: BluetoothDevice, scope: CoroutineScope) {
        val manager = PRXManager(context, scope, ToolboxLogger(context, "PRX"))
        this.manager = manager
        manager.useServer(proximityServerManager)

        manager.dataHolder.status.onEach {
            _data.value = it
            handleLocalAlarm(it)
        }.launchIn(scope)

        manager.connect(device)
            .useAutoConnect(true)
            .retry(3, 100)
            .enqueue()
    }

    private fun handleLocalAlarm(result: BleManagerResult<PRXData>) {
        (result as? SuccessResult<PRXData>)?.let {
            if (it.data.localAlarmLevel != AlarmLevel.NONE) {
                alarmHandler.playAlarm(it.data.localAlarmLevel)
            } else {
                alarmHandler.pauseAlarm()
            }
        }
        (result as? LinkLossResult<PRXData>)?.let {
            alarmHandler.playAlarm(it.data.linkLossAlarmLevel)
        }
    }

    fun enableAlarm() {
        manager?.writeImmediateAlert(true)
    }

    fun disableAlarm() {
        manager?.writeImmediateAlert(false)
    }

    fun release() {
        alarmHandler.releaseAlarm()
        manager?.disconnect()?.enqueue()
        manager = null
    }
}
