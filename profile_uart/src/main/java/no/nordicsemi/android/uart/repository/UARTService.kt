package no.nordicsemi.android.uart.repository

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.service.ForegroundBleService
import no.nordicsemi.android.uart.data.DisconnectCommand
import no.nordicsemi.android.uart.data.SendTextCommand
import no.nordicsemi.android.uart.data.UARTRepository
import no.nordicsemi.android.utils.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
internal class UARTService : ForegroundBleService() {

    @Inject
    lateinit var repository: UARTRepository

    override val manager: UARTManager by lazy { UARTManager(this, scope, repository) }

    override fun onCreate() {
        super.onCreate()

        status.onEach {
            val status = it.mapToSimpleManagerStatus()
            repository.setNewStatus(status)
            stopIfDisconnected(status)
        }.launchIn(scope)

        repository.command.onEach {
            when (it) {
                DisconnectCommand -> stopSelf()
                is SendTextCommand -> manager.send(it.command)
            }.exhaustive
        }.launchIn(scope)
    }
}
