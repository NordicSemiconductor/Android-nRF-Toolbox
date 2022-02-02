package no.nordicsemi.android.hts.repository

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.hts.data.HTSRepository
import no.nordicsemi.android.service.ForegroundBleService
import javax.inject.Inject

@AndroidEntryPoint
internal class HTSService : ForegroundBleService() {

    @Inject
    lateinit var repository: HTSRepository

    override val manager: HTSManager by lazy { HTSManager(this, scope, repository) }

    override fun onCreate() {
        super.onCreate()

        status.onEach {
            val status = it.mapToSimpleManagerStatus()
            repository.setNewStatus(status)
            stopIfDisconnected(status)
        }.launchIn(scope)

        repository.command.onEach {
            stopSelf()
        }.launchIn(scope)
    }
}
