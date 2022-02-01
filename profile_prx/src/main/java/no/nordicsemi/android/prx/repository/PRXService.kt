package no.nordicsemi.android.prx.repository

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.prx.data.*
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.BleServiceStatus
import no.nordicsemi.android.service.ForegroundBleService
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
internal class PRXService : ForegroundBleService() {

    @Inject
    lateinit var repository: PRXRepository

    @Inject
    lateinit var alarmHandler: AlarmHandler

    private var serverManager: ProximityServerManager = ProximityServerManager(this)

    override val manager: PRXManager by lazy {
        PRXManager(this, repository).apply {
            useServer(serverManager)
        }
    }

    override fun onCreate() {
        super.onCreate()

        serverManager.open()

        status.onEach {
            val bleStatus = when (it) {
                BleServiceStatus.CONNECTING -> BleManagerStatus.CONNECTING
                BleServiceStatus.OK -> BleManagerStatus.OK
                BleServiceStatus.DISCONNECTED -> {
                    scope.close()
                    stopSelf()
                    BleManagerStatus.DISCONNECTED
                }
                BleServiceStatus.LINK_LOSS -> null
            }.exhaustive
            bleStatus?.let { repository.setNewStatus(it) }

            if (BleServiceStatus.LINK_LOSS == it) {
                repository.setLocalAlarmLevel(repository.data.value.linkLossAlarmLevel)
            }
        }.launchIn(scope)

        repository.command.onEach {
            when (it) {
                DisableAlarm -> manager.writeImmediateAlert(false)
                EnableAlarm -> manager.writeImmediateAlert(true)
                Disconnect -> stopSelf()
            }.exhaustive
        }.launchIn(scope)

        repository.data.onEach {
            if (it.localAlarmLevel != AlarmLevel.NONE) {
                alarmHandler.playAlarm(it.localAlarmLevel)
            } else {
                alarmHandler.pauseAlarm()
            }
        }.launchIn(scope)
    }

    override fun shouldAutoConnect(): Boolean {
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        alarmHandler.releaseAlarm()
        serverManager.close()
    }
}
