package no.nordicsemi.android.hrs.service

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.hrs.data.HRSRepository
import no.nordicsemi.android.service.ForegroundBleService
import javax.inject.Inject

@AndroidEntryPoint
internal class HRSService : ForegroundBleService() {

    @Inject
    lateinit var repository: HRSRepository

    override val manager: HRSManager by lazy { HRSManager(this, scope, repository) }

    override fun onCreate() {
        super.onCreate()

        repository.command.onEach {
            stopSelf()
        }.launchIn(scope)
    }
}
