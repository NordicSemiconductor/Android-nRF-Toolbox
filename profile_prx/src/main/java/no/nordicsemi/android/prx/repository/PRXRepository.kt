package no.nordicsemi.android.prx.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.logger.NordicLogger
import no.nordicsemi.android.logger.NordicLoggerFactory
import no.nordicsemi.android.prx.data.AlarmLevel
import no.nordicsemi.android.prx.data.PRXData
import no.nordicsemi.android.prx.data.PRXManager
import no.nordicsemi.android.prx.data.ProximityServerManager
import no.nordicsemi.android.service.*
import no.nordicsemi.android.theme.view.StringConst
import no.nordicsemi.ui.scanner.DiscoveredBluetoothDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PRXRepository @Inject internal constructor(
    @ApplicationContext
    private val context: Context,
    private val serviceManager: ServiceManager,
    private val proximityServerManager: ProximityServerManager,
    private val alarmHandler: AlarmHandler,
    private val loggerFactory: NordicLoggerFactory,
    private val stringConst: StringConst
) {

    private var manager: PRXManager? = null
    private var logger: NordicLogger? = null

    private val _data = MutableStateFlow<BleManagerResult<PRXData>>(IdleResult())
    internal val data = _data.asStateFlow()

    val isRunning = data.map { it.isRunning() }
    val hasBeenDisconnectedWithoutLinkLoss = data.map { it.hasBeenDisconnectedWithoutLinkLoss() }

    fun launch(device: DiscoveredBluetoothDevice) {
        serviceManager.startService(PRXService::class.java, device)
        proximityServerManager.open()
    }

    fun start(device: DiscoveredBluetoothDevice, scope: CoroutineScope) {
        val createdLogger = loggerFactory.create(stringConst.APP_NAME, "PRX", device.address()).also {
            logger = it
        }
        val manager = PRXManager(context, scope, createdLogger)
        this.manager = manager
        manager.useServer(proximityServerManager)

        manager.dataHolder.status.onEach {
            _data.value = it
            handleLocalAlarm(it)
        }.launchIn(scope)

        manager.connect(device.device)
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
            val alarmLevel = it.data?.linkLossAlarmLevel ?: AlarmLevel.HIGH
            alarmHandler.playAlarm(alarmLevel)
        }
    }

    fun enableAlarm() {
        manager?.writeImmediateAlert(true)
    }

    fun disableAlarm() {
        manager?.writeImmediateAlert(false)
    }

    fun openLogger() {
        logger?.openLogger()
    }

    fun release() {
        disableAlarm()
        manager?.disconnect()?.enqueue()
        manager = null
        logger = null
    }
}
