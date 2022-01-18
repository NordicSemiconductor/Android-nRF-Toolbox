package no.nordicsemi.android.rscs.service

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.rscs.data.RSCSRepository
import no.nordicsemi.android.service.ForegroundBleService
import javax.inject.Inject

@AndroidEntryPoint
internal class RSCSService : ForegroundBleService() {

    @Inject
    lateinit var repository: RSCSRepository

    override val manager: RSCSManager by lazy { RSCSManager(this, repository) }

    override fun onCreate() {
        super.onCreate()

        status.onEach {
            repository.setNewStatus(it)
        }.launchIn(scope)

        repository.command.onEach {
            stopSelf()
        }.launchIn(scope)
    }
}
