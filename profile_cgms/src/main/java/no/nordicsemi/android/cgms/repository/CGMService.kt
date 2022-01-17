package no.nordicsemi.android.cgms.repository

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.cgms.data.CGMRepository
import no.nordicsemi.android.cgms.data.CGMServiceCommand
import no.nordicsemi.android.service.ForegroundBleService
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
internal class CGMService : ForegroundBleService() {

    @Inject
    lateinit var repository: CGMRepository

    override val manager: CGMManager by lazy { CGMManager(this, repository) }

    override fun onCreate() {
        super.onCreate()

        status.onEach {
            repository.setNewStatus(it)
        }.launchIn(scope)

        repository.command.onEach {
            when (it) {
                CGMServiceCommand.REQUEST_ALL_RECORDS -> manager.requestAllRecords()
                CGMServiceCommand.REQUEST_LAST_RECORD -> manager.requestLastRecord()
                CGMServiceCommand.REQUEST_FIRST_RECORD -> manager.requestFirstRecord()
                CGMServiceCommand.DISCONNECT -> stopSelf()
            }.exhaustive
        }.launchIn(scope)
    }
}
