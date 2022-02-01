package no.nordicsemi.android.hrs.service

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.hrs.data.HRSRepository
import no.nordicsemi.android.service.BleManagerStatus
import no.nordicsemi.android.service.BleServiceStatus
import no.nordicsemi.android.service.ForegroundBleService
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
internal class HRSService : ForegroundBleService() {

    @Inject
    lateinit var repository: HRSRepository

    override val manager: HRSManager by lazy { HRSManager(this, repository) }

    override fun onCreate() {
        super.onCreate()

        status.onEach {
            val status = it.mapToSimpleManagerStatus()
            repository.setNewStatus(status)
            if (status == BleManagerStatus.DISCONNECTED) {
                scope.close()
            }
        }.launchIn(scope)

        repository.command.onEach {
            stopSelf()
        }.launchIn(scope)
    }
}
