package no.nordicsemi.android.csc.repository

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.csc.data.CSCRepository
import no.nordicsemi.android.csc.data.DisconnectCommand
import no.nordicsemi.android.csc.data.SetWheelSizeCommand
import no.nordicsemi.android.service.ForegroundBleService
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
internal class CSCService : ForegroundBleService() {

    @Inject
    lateinit var repository: CSCRepository

    override val manager: CSCManager by lazy { CSCManager(this, repository) }

    override fun onCreate() {
        super.onCreate()

        repository.command.onEach {
            when (it) {
                DisconnectCommand -> stopSelf()
                is SetWheelSizeCommand -> manager.setWheelSize(it.size)
            }.exhaustive
        }.launchIn(scope)
    }
}
